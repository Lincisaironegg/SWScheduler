package SchedulerStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Server.Cpu;
import Server.Server;
import DAGParser.DAG;
import DAGParser.Edge;
import DAGParser.Task;


public class FIFO {

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
		FIFO o = new FIFO(adj, par, nT, nS, cost, null, listc);
		o.run();
	}

	// DLP类参数
	ArrayList<Cpu> listc;
	int num_tasks, num_servers;
	List<Edge> adj[];
	List<Edge> adj_par[];
	int task_sz[]; // 任务的大小，以MB或GB为单位，及执行任务需要的空间
	int cost[][]; // 任务i在服务j上执行需要花费的时间
	// 全参构造

	private FIFO(List<Edge> adj[], List<Edge> p[], int nt, int ns, int c[][], int tsz[], ArrayList<Cpu> listc) {
		this.adj = adj.clone();
		adj_par = p;
		num_tasks = nt;
		num_servers = ns;
		cost = c;
		task_sz = tsz;
		this.listc = listc;

	}

	// 将adj[]的id与DLP值封装进task_node类里，进行排序

	int max_par[];
	int spend = 0;

	void run() {
		int i;
		int time = 0;
		int[] a = new int[20];// 用于标记该节点是否分配，1为已分配，0为未分配

		for (i = 1; i < num_tasks; i++) {
			int min_j = 1 + (int) (Math.random() * 4);// 随机部署到一个服务上

			if (a[i] != 1) {

				a[i] = 1;

				System.out.printf("---- Adding Node %d to server %d at time %d ----\n", i, min_j, time);
				spend += listc.get(min_j - 1).getSpend();
				time += cost[i][min_j];
				int sz;
				sz = adj[i].size();
				for (int e = 0; e < sz; e++) {
					Edge ed = adj[i].get(e);
					if (a[ed.getTo()] != 1) {// 部署该节点的子节点

						int min_j1 = 1 + (int) (Math.random() * 4);
						System.out.printf("---- Adding Node %d to server %d at time %d ----\n", ed.getTo(), min_j1,
								time);
						spend += listc.get(min_j - 1).getSpend();
						time += ed.getSize() + cost[ed.getTo()][min_j1];
						a[ed.getTo()] = 1;
					}

					time += ed.getSize();
					a[ed.getTo()] = 1;
				}

			}
		}
		System.out.printf("\n   Total time taken : %d,total spend : %d ", time, spend);
	}

}