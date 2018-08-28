package SchedulerStrategy;

import java.io.File;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Server.Cpu;
import Server.Server;
import util.Dfs;
import util.task_node;
import DAGParser.DAG;
import DAGParser.Edge;
import DAGParser.Task;

//大部分是上周的代码，优化封装后进行了微调
public class DLP {

	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String[] args) {
		// 初始化一个Map用于接收调度结果
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		// 调用Server类，获取cpu相关信息（bw带宽（单位MBps），serv_num即cpu数量，以及存放cpu的list
		Server Serv = new Server();
		int Bw = Serv.getBw();
		int nS = Serv.getSer_num();
		ArrayList<Cpu> listc = Serv.getListc();
		// 调用DAG类，获取DAG有向无关图的相关信息，包括task的id，大小size（单位MI），task
		File file = new File("C:\\Users\\bin\\Desktop\\model.xml");
		DAG dag = DAG.dagReader(file);
		int nT = dag.getTask_num();
		// 通过 （传输数据量的大小/带宽） 来计算出每个边的cost
		// 所对应的边的列表liste，以及边的指向to，边的传输量size（单位MB）
		ArrayList<Task> list = dag.getList();
		for (Task i : list) {
			for (Edge e : i.getListe()) {
				e.setSize(e.getSize() / Bw);
			}
		}
		// 创建正向边的集合adj与反向边的集合par
		List<Edge> adj[] = new ArrayList[nT + 1];
		List<Edge> par[] = new ArrayList[nT + 1];
		// 初始化
		for (int i = 1; i <= nT; i++) {
			adj[i] = new ArrayList<Edge>();
			par[i] = new ArrayList<Edge>();
		}
		// 遍历赋值
		for (int j = 1; j <= nT; j++) {
			adj[j] = list.get(j - 1).getListe();

		}
		// 给par赋值
		for (int j = 1; j <= nT; j++) {
			int sz = adj[j].size(), x = 0;
			for (; x < sz; x++) {
				Edge e = adj[j].get(x);
				par[e.getTo()].add(new Edge(j, e.getSize()));
			}

		}
		// 初始化cost矩阵，并根据 cost=tasksize/cpu.mips来计算每个节点在每个服务上的cost
		int[][] cost = new int[nT + 1][nS + 1];
		for (int i = 1; i <= nT; i++) {
			for (int j = 1; j <= nS; j++) {
				cost[i][j] = (int) (list.get(i - 1).getSize() / listc.get(j - 1).getMips());
			}
		}
		// 初始化DLP类，并开始运行算法
		DLP o = new DLP(adj, par, nT, nS, cost, null,listc);
		sche = o.run();
	}
	// DLP类参数
	ArrayList<Cpu> listc;
	int num_tasks, num_servers;
	List<Edge> adj[];
	List<Edge> adj_par[];
	int task_sz[]; // 任务的大小，以MB或GB为单位，及执行任务需要的空间
	int cost[][]; // 任务i在服务j上执行需要花费的时间
	// 全参构造

	private DLP(List<Edge> adj[], List<Edge> p[], int nt, int ns, int c[][], int tsz[],ArrayList<Cpu> listc) {
		this.adj = adj.clone();
		adj_par = p;
		num_tasks = nt;
		num_servers = ns;
		cost = c;
		task_sz = tsz;
		this.listc = listc;

	}

	// 将adj[]的id与DLP值封装进task_node类里，进行排序
	task_node arr[];
	int max_par[];
	int spend = 0;
	private Map<Map<Integer, Integer>, Integer> run() {
		// 初始化存储task-cpu的map
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		// 初始化map存储result-time对
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		int i, j;
		//调用DFS类对arr进行排序
		Dfs dfs = new Dfs(num_tasks,num_servers,cost,adj);
		arr = dfs.getDfs();
		// 动态存储某任务最早能使用的时间
		int curr_state[] = new int[num_servers + 1];
		// 动态存储某任务的实际完成时间AFS
		int finish[] = new int[num_tasks + 1];
		// 开始将任务与服务一一对应
		for (i = 0; i < num_tasks; i++) {
			// System.out.println(arr[i].dlp);
			int id = arr[i].getId(), min = Integer.MAX_VALUE, min_j = -1, max = 0;
			// 遍历得到任务最早开始时间，即所有父节点的AFT与对应边的cost的最大值
			for (j = 0; j < adj_par[id].size(); j++) {
				Edge e = adj_par[id].get(j);
				if (e.getSize() + finish[e.getTo()] > max) {
					max = (int) e.getSize() + finish[e.getTo()];
				}
			}
			// 服务j最早能使用的时间和任务最早开始时间的最大值加上本任务在本服务的花费，遍历后得到最小值
			for (j = 1; j <= num_servers; j++) {
				if (Math.max(curr_state[j], max) + cost[id][j] < min) {
					min = Math.max(curr_state[j], max) + cost[id][j];
					min_j = j;
				}
			}

			curr_state[min_j] = min;
			finish[id] = curr_state[min_j];
			System.out.println();
			System.out.printf("---- Adding Node %d to server %d at time %d ----", id, min_j, min - cost[id][min_j]);
			spend += listc.get(min_j-1).getSpend();
			result.put(id, min_j);
			sche.put(result, (Integer) (min - cost[id][min_j]));
		}
		int max = 0;
		for (i = 0; i < curr_state.length; i++)
			if (curr_state[i] > max)
				max = curr_state[i];
		
		

		System.out.printf("\n   Total time taken : %d,total spend : %d ", max,spend);
		return sche;
	}

	// 递归地计算出每个节点的dlp值，
	// 用以进行排序 dlp=任务在每个服务上花费的平均值+max（e.to.dlp+e.weight)，
	// 与最晚执行时间呈负相关



}