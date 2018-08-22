package SchedulerStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import DAGParser.DAG;
import DAGParser.Edge;
import DAGParser.Task;
import Server.Cpu;
import Server.Server;

public class BHEFT {
	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String[] args) {
		// 初始化一个Map用于接收调度结果
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		// 调用Server类，获取cpu相关信息（bw带宽（单位MBps），serv_num即cpu数量，以及存放cpu的list
		Server Serv = new Server();
		int Bw = Serv.getBw();
		int nS = Serv.getSer_num();
		ArrayList<Cpu> listc = Serv.getListc();
		// 调用DAG类，获取DAG有向无关图的相关信息，包括task的id，大小size（单位MI），tasklist,buget预算
		File file = new File("C:\\Users\\bin\\Desktop\\model.xml");
		DAG dag = DAG.dagReader(file);
		//System.out.println(dag.toString());
		int nT = dag.getTask_num();
		double Buget = dag.getBuget();
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
		BHEFT o = new BHEFT(adj, par, nT, nS, cost, null, listc, Buget);
		sche = o.run();
	}

	// DLP类参数
	double Buget;
	ArrayList<Cpu> listc;
	int num_tasks, num_servers;
	List<Edge> adj[];
	List<Edge> adj_par[];
	int task_sz[]; // 任务的大小，以MB或GB为单位，及执行任务需要的空间
	int cost[][]; // 任务i在服务j上执行需要花费的时间
	// 全参构造
	private BHEFT(List<Edge> adj[], List<Edge> p[], int nt, int ns, int c[][], int tsz[], ArrayList<Cpu> listc,
			double Buget) {
		this.adj = adj.clone();
		adj_par = p;
		num_tasks = nt;
		num_servers = ns;
		cost = c;
		task_sz = tsz;
		this.listc = listc;
		this.Buget = Buget;

	}

	task_node arr[];
	int max_par[];
	int spend = 0;

	private Map<Map<Integer, Integer>, Integer> run() {
		// 初始化存储task-cpu的map
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		// 初始化map存储result-time对
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		// 初始化arr[]数组
		arr = new task_node[num_tasks];
		// 遍历arr数组并初始化task_node
		int i, j;
		for (i = 0; i < num_tasks; i++)
			arr[i] = new task_node(i + 1);
		// 遍历任务结点，并调用dfs方法
		for (i = 0; i < num_tasks; i++)
			if (arr[i].dlp == 0)
				dfs(arr[i].id);
		// 使用重写的排序规则进行排序
		Arrays.sort(arr);
		// 动态存储某任务最早能使用的时间
		int curr_state[] = new int[num_servers + 1];
		// 动态存储某任务的实际完成时间AFS
		int finish[] = new int[num_tasks + 1];
		// 计算所有服务的平均花费,以及最便宜的服务
		double avg = 0.0;
		int MIN = Integer.MAX_VALUE;
		Cpu C_min = listc.get(0);
		for (Cpu c : listc) {
			avg += c.getSpend();
			if (c.getSpend() < MIN) {
				MIN = (int) c.getSpend();
				C_min = c;
			}
		}
		avg = avg / listc.size();
		
		// Spend用来存储当前花费,spend_pre用来存储未分配任务的平均花费
		double spend = 0.0;
		double spend_pre = avg * arr.length;
		
		// SAB Spare Application Budget 表示预测的剩余预算
		double SAB;
		// CTB Current Task Budget 表示当前任务允许的花费
		double CTB;
		// AF Adjustment Factor AF是一个值，它的作用是调整SAB对CTB的影响
		double AF;
		// 开始将任务与服务一一对应
		for (i = 0; i < num_tasks; i++) {
			int id = arr[i].id, min = Integer.MAX_VALUE, min_j = -1, max = 0;
			SAB = Buget - spend - spend_pre;
			if (SAB >= 0) {
				AF = avg / spend_pre;

			} else {
				AF = 0;
			}
			CTB = avg + SAB * AF;
			//System.out.println("\n"+CTB+" "+SAB+" "+AF);
			// 遍历服务，筛选出符合花费要求的服务S
			List<Cpu> S = new ArrayList<Cpu>();
			for (Cpu c : listc) {
				if (c.getSpend() <= CTB) {
					S.add(c);
				}
			}
			// 如果SAB即预测剩余预算>=0,则选择最快的服务，否则选择最便宜的
			if (SAB >= 0) {

				// 遍历得到任务最早开始时间，即所有父节点的AFT与对应边的cost的最大值
				for (j = 0; j < adj_par[id].size(); j++) {
					Edge e = adj_par[id].get(j);
					if (e.getSize() + finish[e.getTo()] > max) {
						max = (int) e.getSize() + finish[e.getTo()];
					}
				}
				// 服务j最早能使用的时间和任务最早开始时间的最大值加上本任务在本服务的花费，遍历后得到最小值
				for (Cpu c:S) {
					if (Math.max(curr_state[c.getId()], max) + cost[id][c.getId()] < min) {
						min = Math.max(curr_state[c.getId()], max) + cost[id][c.getId()];
						min_j = c.getId();

					}
				}
			} else {
				// 遍历得到任务最早开始时间，即所有父节点的AFT与对应边的cost的最大值
				for (j = 0; j < adj_par[id].size(); j++) {
					Edge e = adj_par[id].get(j);
					if (e.getSize() + finish[e.getTo()] > max) {
						max = (int) e.getSize() + finish[e.getTo()];
					}
				}
				//计算将task部署到最便宜服务上时的实际完成时间
				min = Math.max(curr_state[C_min.getId()], max) + cost[id][C_min.getId()];
				min_j = C_min.getId();
				result.put(id, min_j);
				

			}
			//更新spend和spend_pre，将结果存入Map
			spend += listc.get(min_j - 1).getSpend();
			spend_pre -= avg;
			curr_state[min_j] = min;
			finish[id] = curr_state[min_j];
			System.out.println();
			System.out.printf("---- Adding Node %d to server %d at time %d ----", id, min_j, min - cost[id][min_j]);

			result.put(id, min_j);
			sche.put(result, (Integer) (min - cost[id][min_j]));

		}
		//打印测试
		int Max = 0;
		for (i = 0; i < curr_state.length; i++)
			if (curr_state[i] > Max)
				Max = curr_state[i];

		System.out.printf("\n   Total time taken : %d,total spend : %.2f ", Max, spend);
		return sche;
	}

	private float dfs(int node_id) {
		if (arr[node_id - 1].dlp != 0)
			return arr[node_id - 1].dlp;
		// 出口结点的dlp为每个服务上花费的平均值
		if (adj[node_id].size() == 0) {
			float avg = 0;
			for (int j = 1; j <= num_servers; j++) {

				avg += cost[node_id][j];
			}
			return arr[node_id - 1].dlp = avg / num_servers;
		}

		int i, sz = adj[node_id].size();
		float max = -1;
		// 遍历节点的每个边，计算出所有子节点DLP与通向该节点路径cost之和的最大值
		for (i = 0; i < sz; i++) {
			Edge e = adj[node_id].get(i);
			max = Math.max(dfs(e.getTo()) + e.getSize(), max);
		}
		arr[node_id - 1].dlp = max;
		float avg = 0;
		// 计算该节点在每个服务器上的平均cost
		for (int j = 1; j <= num_servers; j++)
			avg += cost[node_id][j];

		return arr[node_id - 1].dlp += (avg / num_servers);
	}

	private class task_node implements Comparable<task_node> {
		int id;
		float dlp;

		task_node(int x) {
			id = x;
			dlp = 0;
		}

		@Override
		public int compareTo(task_node arg) {

			if (this.dlp < arg.dlp)
				return 1;
			if (this.dlp > arg.dlp)
				return -1;

			return 0;
		}
	}
}
