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
		// ��ʼ��һ��Map���ڽ��յ��Ƚ��
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		// ����Server�࣬��ȡcpu�����Ϣ��bw������λMBps����serv_num��cpu�������Լ����cpu��list
		File f = new File("C:\\Users\\bin\\Desktop\\info_vn000182.xml");
		Server Serv = Server.serverReader(f);
		int Bw = Serv.getBw();
		int nS = Serv.getSer_num();
		ArrayList<Cpu> listc = Serv.getListc();
		// ����DAG�࣬��ȡDAG�����޹�ͼ�������Ϣ������task��id����Сsize����λMI����tasklist,bugetԤ��
		File file = new File("C:\\Users\\bin\\Desktop\\model.xml");
		DAG dag = DAG.dagReader(file);
		//System.out.println(dag.toString());
		int nT = dag.getTask_num();
		double Buget = dag.getBuget();
		int DL = dag.getDl();
		// ͨ�� �������������Ĵ�С/���� �������ÿ���ߵ�cost
		// ����Ӧ�ıߵ��б�liste���Լ��ߵ�ָ��to���ߵĴ�����size����λMB��
		ArrayList<Task> list = dag.getList();
		for (Task i : list) {
			for (Edge e : i.getListe()) {
				e.setSize(e.getSize() / Bw);
			}
		}
		// ��������ߵļ���adj�뷴��ߵļ���par
		List<Edge> adj[] = new ArrayList[nT + 1];
		List<Edge> par[] = new ArrayList[nT + 1];
		// ��ʼ��
		for (int i = 1; i <= nT; i++) {
			adj[i] = new ArrayList<Edge>();
			par[i] = new ArrayList<Edge>();
		}
		// ������ֵ
		for (int j = 1; j <= nT; j++) {
			adj[j] = list.get(j - 1).getListe();

		}
		// ��par��ֵ
		for (int j = 1; j <= nT; j++) {
			int sz = adj[j].size(), x = 0;
			for (; x < sz; x++) {
				Edge e = adj[j].get(x);
				par[e.getTo()].add(new Edge(j, e.getSize()));
			}

		}
		// ��ʼ��cost���󣬲����� cost=tasksize/cpu.mips������ÿ���ڵ���ÿ�������ϵ�cost
		int[][] cost = new int[nT + 1][nS + 1];
		for (int i = 1; i <= nT; i++) {
			for (int j = 1; j <= nS; j++) {
				cost[i][j] = (int) (list.get(i - 1).getSize() / listc.get(j - 1).getMips());
			}
		}
		// ��ʼ��DLP�࣬����ʼ�����㷨
		DBCS o = new DBCS(adj, par, nT, nS, cost, null, listc, Buget,DL);
		sche = o.run();
	}

	// DLP�����
	int DL;
	double Buget;
	ArrayList<Cpu> listc;
	int num_tasks, num_servers;
	List<Edge> adj[];
	List<Edge> adj_par[];
	int task_sz[]; // ����Ĵ�С����MB��GBΪ��λ����ִ��������Ҫ�Ŀռ�
	int cost[][]; // ����i�ڷ���j��ִ����Ҫ���ѵ�ʱ��
	// ȫ�ι���
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
		// ��ʼ���洢task-cpu��map
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		// ��ʼ��map�洢result-time��
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();

		//����DFS���arr��������
		int i, j;
		Dfs dfs = new Dfs(num_tasks,num_servers,cost,adj);
		arr = dfs.getDfs();
		// ʹ����д����������������
		Arrays.sort(arr);
		// �������з����ƽ������,�Լ�����˵ķ���
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
		//����ÿ���ڵ���ӽ�ֹ����subDl
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
		//��ʼ����spend
		double spareSpend = this.Buget - C_min.getSpend()*num_tasks;
		//��ʼ��CL
		double CL;
		// ��̬�洢ĳ����������ʹ�õ�ʱ��
		int curr_state[] = new int[num_servers + 1];
		// ��̬�洢ĳ�����ʵ�����ʱ��AFS
		int finish[] = new int[num_tasks + 1];

		
		// Spend�����洢��ǰ����,spend_pre�����洢δ���������ƽ������
		double spend = 0.0;
		
		// ��ʼ�����������һһ��Ӧ
		for (i = 0; i < num_tasks; i++) {
			
			int id = arr[i].getId(), min = Integer.MAX_VALUE, fast_j = -1, slow_j = -1,max = 0,fin_j = -1,Max = 0;
			CL = C_min.getSpend() + spareSpend;
			
			//�洢����Ԥ����˵���õ�cpu
			List<Cpu> listA = new ArrayList<Cpu>();
			for(Cpu cp : listc) {
				if(cp.getSpend() <= CL) {
					listA.add(cp);
				}
			}
			//������õ�cpu�л�������cpu
			
			Cpu c_exp;
			double exp = -1;
			for(Cpu ce : listA) {
				if(ce.getSpend() > exp) {
					exp = ce.getSpend();
					c_exp = ce;
				}
			}
			//����FT���ĺ�������
			// �����õ��������翪ʼʱ�䣬�����и��ڵ��AFT���Ӧ�ߵ�cost�����ֵ
			for (j = 0; j < adj_par[id].size(); j++) {
				Edge e = adj_par[id].get(j);
				if (e.getSize() + finish[e.getTo()] > Max) {
					Max = (int) e.getSize() + finish[e.getTo()];
				}
			}
			// ����j������ʹ�õ�ʱ����������翪ʼʱ������ֵ���ϱ������ڱ�����ĺ�ʱ��������õ���Сֵ�����ֵ
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
			//����listA
			double Q_max = -(Double.MAX_VALUE);
			for(Cpu cf : listA) {
				//Q(ti,pj) = Timeq + Costq*Costcheapest/Buget

				//Timeq = (��*subDL-costTime(i,j))/(costTimemax-costTimemin)

				//Costq = (spendBest-spend(i,j))/(spendMax-spendMin)	
				//����TimeQֵ(�����ж�k��ֵ)
				int k;
				if(Math.max(curr_state[cf.getId()], Max) + cost[id][cf.getId()] < arr[i].getSubDl()) {
					k = 1;
				}else {
					k=0;
				}
				int TimeQ = (k*arr[i].getSubDl()-cost[arr[i].getId()][cf.getId()])/(max-min);
				//����SpendQֵ
				double SpendQ = (listc.get(fast_j-1).getSpend()-cf.getSpend())/(exp-MIN);
				
				
				//����Qֵ����ѡ������
				
				double Q = TimeQ + SpendQ*MIN*num_tasks/Buget;
				if(Q > Q_max) {
					
					Q_max = Q;
					fin_j = cf.getId();
					
				}
			}
			
			//������bushuzai�÷�����
			//����spend��spend_pre�����������Map
			spareSpend -= listc.get(fin_j-1).getSpend()-C_min.getSpend();
			spend += listc.get(fin_j - 1).getSpend();
			curr_state[fin_j] = Math.max(curr_state[fin_j], Max) + cost[id][fin_j];
			finish[id] = curr_state[fin_j];
			System.out.println();
			System.out.printf("---- Adding Node %d to server %d at time %d ----", id, fin_j, min - cost[id][fin_j]);

			result.put(id, fin_j);
			sche.put(result, (Integer) (min - cost[id][fin_j]));


		}
		//��ӡ����
		int fin_time = 0;
		for (i = 0; i < curr_state.length; i++)
			if (curr_state[i] > fin_time)
				fin_time = curr_state[i];

		System.out.printf("\n   Total time taken : %d,total spend : %.2f ", fin_time, spend);
		return sche;
	}
	


	


}
 