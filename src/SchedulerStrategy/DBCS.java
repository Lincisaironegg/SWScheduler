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
import util.Dfs;
import util.task_node;

public class DBCS {
	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String[] args) {
		// 初始化一个Map用于接收调度结果
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		// 调用Server类，获取cpu相关信息（bw带宽（单位MBps），serv_num即cpu数量，以及存放cpu的list
		File f = new File("C:\\Users\\bin\\Desktop\\info_vn000182.xml");
		Server Serv = Server.serverReader(f);
		int Bw = Serv.getBw();
		int nS = Serv.getSer_num();
		ArrayList<Cpu> listc = Serv.getListc();
		// 调用DAG类，获取DAG有向无关图的相关信息，包括task的id，大小size（单位MI），tasklist,buget预算
		File file = new File("C:\\Users\\bin\\Desktop\\model.xml");
		DAG dag = DAG.dagReader(file);
		//System.out.println(dag.toString());
		int nT = dag.getTask_num();
		double Buget = dag.getBuget();
		int DL = dag.getDl();
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
		DBCS o = new DBCS(adj, par, nT, nS, cost, null, listc, Buget,DL);
		sche = o.run();
	}

	// DLP类参数
	int DL;
	double Buget;
	ArrayList<Cpu> listc;
	int num_tasks, num_servers;
	List<Edge> adj[];
	List<Edge> adj_par[];
	int task_sz[]; // 任务的大小，以MB或GB为单位，及执行任务需要的空间
	int cost[][]; // 任务i在服务j上执行需要花费的时间
	// 全参构造
	private DBCS(List<Edge> adj[], List<Edge> p[], int nt, int ns, int c[][], int tsz[], ArrayList<Cpu> listc,
			double Buget,int DL) {
		this.adj = adj.clone();
		adj_par = p;
		num_tasks = nt;
		num_servers = ns;
		cost = c;
		task_sz = tsz;
		this.listc = listc;
		this.Buget = Buget;
		this.DL = DL;

	}

	task_node arr[];
	int max_par[];
	int spend = 0;
	@SuppressWarnings("unused")
	private Map<Map<Integer, Integer>, Integer> run() {
		// 初始化存储task-cpu的map
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		// 初始化map存储result-time对
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();

		//调用DFS类对arr进行排序
		int i, j;
		Dfs dfs = new Dfs(num_tasks,num_servers,cost,adj);
		arr = dfs.getDfs();
		// 使用重写的排序规则进行排序
		Arrays.sort(arr);
		// 计算所有服务的平均花费,以及最便宜的服务
		double avg = 0.0;
		int MIN = Integer.MAX_VALUE;
		int MAX = -1;
		Cpu C_min = listc.get(0);
		Cpu C_max = listc.get(0);
		for (Cpu c : listc) {
			avg += c.getSpend();
			if (c.getSpend() < MIN) {
				MIN = (int) c.getSpend();
				C_min = c;
			}
			if(c.getSpend() > MAX) {
				MAX = (int) c.getSpend();
				C_max = c;
			}
		}
		avg = avg / listc.size();
		//计算每个节点的子截止日期subDl
		int deadline = this.DL;
		for(int t = num_tasks - 1;t >= 0;t--) {
			if(t == (num_tasks-1)) {
				arr[t].setSubDl(this.DL);
			}else {
				int m = Integer.MAX_VALUE;
				for(Edge e : adj[arr[t].getId()]) {
					if(arr[e.getTo()-1].getSubDl() - e.getSize() - cost[e.getTo()][C_max.getId()] < m) {
						m = (int)(arr[e.getTo()-1].getSubDl() - e.getSize() - cost[e.getTo()][C_max.getId()]);
					}
				}
				arr[t].setSubDl(m);
				
				
			}
		}
		//初始化△spend
		double spareSpend = this.Buget - C_min.getSpend()*num_tasks;
		//初始化CL
		double CL;
		// 动态存储某任务最早能使用的时间
		int curr_state[] = new int[num_servers + 1];
		// 动态存储某任务的实际完成时间AFS
		int finish[] = new int[num_tasks + 1];

		
		// Spend用来存储当前花费,spend_pre用来存储未分配任务的平均花费
		double spend = 0.0;
		
		// 开始将任务与服务一一对应
		for (i = 0; i < num_tasks; i++) {
			
			int id = arr[i].getId(), min = Integer.MAX_VALUE, fast_j = -1, slow_j = -1,max = 0,fin_j = -1,Max = 0;
			CL = C_min.getSpend() + spareSpend;
			
			//存储根据预算来说可用的cpu
			List<Cpu> listA = new ArrayList<Cpu>();
			for(Cpu cp : listc) {
				if(cp.getSpend() <= CL) {
					listA.add(cp);
				}
			}
			//计算可用的cpu中花费最大的cpu
			
			Cpu c_exp;
			double exp = -1;
			for(Cpu ce : listA) {
				if(ce.getSpend() > exp) {
					exp = ce.getSpend();
					c_exp = ce;
				}
			}
			//计算FT最快的和最慢的
			// 遍历得到任务最早开始时间，即所有父节点的AFT与对应边的cost的最大值
			for (j = 0; j < adj_par[id].size(); j++) {
				Edge e = adj_par[id].get(j);
				if (e.getSize() + finish[e.getTo()] > Max) {
					Max = (int) e.getSize() + finish[e.getTo()];
				}
			}
			// 服务j最早能使用的时间和任务最早开始时间的最大值加上本任务在本服务的耗时，遍历后得到最小值和最大值
			for (Cpu c:listA) {
				if (Math.max(curr_state[c.getId()], Max) + cost[id][c.getId()] < min) {
					min = Math.max(curr_state[c.getId()], Max) + cost[id][c.getId()];
					fast_j = c.getId();
				}
				if(Math.max(curr_state[c.getId()], Max) + cost[id][c.getId()] > max) {
					max = Math.max(curr_state[c.getId()], Max) + cost[id][c.getId()];
					slow_j = c.getId();
				}
			}
			//遍历listA
			double Q_max = -(Double.MAX_VALUE);
			for(Cpu cf : listA) {
				//Q(ti,pj) = Timeq + Costq*Costcheapest/Buget

				//Timeq = (Ω*subDL-costTime(i,j))/(costTimemax-costTimemin)

				//Costq = (spendBest-spend(i,j))/(spendMax-spendMin)	
				//计算TimeQ值(首先判断k的值)
				int k;
				if(Math.max(curr_state[cf.getId()], Max) + cost[id][cf.getId()] < arr[i].getSubDl()) {
					k = 1;
				}else {
					k=0;
				}
				int TimeQ = (k*arr[i].getSubDl()-cost[arr[i].getId()][cf.getId()])/(max-min);
				//计算SpendQ值
				double SpendQ = (listc.get(fast_j-1).getSpend()-cf.getSpend())/(exp-MIN);
				
				
				//计算Q值并且选个最大的
				
				double Q = TimeQ + SpendQ*MIN*num_tasks/Buget;
				if(Q > Q_max) {
					
					Q_max = Q;
					fin_j = cf.getId();
					
				}
			}
			
			//将任务bushuzai该服务上
			//更新spend和spend_pre，将结果存入Map
			spareSpend -= listc.get(fin_j-1).getSpend()-C_min.getSpend();
			spend += listc.get(fin_j - 1).getSpend();
			curr_state[fin_j] = Math.max(curr_state[fin_j], Max) + cost[id][fin_j];
			finish[id] = curr_state[fin_j];
			System.out.println();
			System.out.printf("---- Adding Node %d to server %d at time %d ----", id, fin_j, min - cost[id][fin_j]);

			result.put(id, fin_j);
			sche.put(result, (Integer) (min - cost[id][fin_j]));


		}
		//打印测试
		int fin_time = 0;
		for (i = 0; i < curr_state.length; i++)
			if (curr_state[i] > fin_time)
				fin_time = curr_state[i];

		System.out.printf("\n   Total time taken : %d,total spend : %.2f ", fin_time, spend);
		return sche;
	}
	


	


}
 