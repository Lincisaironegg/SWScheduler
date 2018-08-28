package SchedulerStrategy;

import java.io.File;
import java.util.ArrayList;
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

public class BHEFT {
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
		BHEFT o = new BHEFT(adj, par, nT, nS, cost, null, listc, Buget);
		sche = o.run();
	}

	// DLP�����
	double Buget;
	ArrayList<Cpu> listc;
	int num_tasks, num_servers;
	List<Edge> adj[];
	List<Edge> adj_par[];
	int task_sz[]; // ����Ĵ�С����MB��GBΪ��λ����ִ��������Ҫ�Ŀռ�
	int cost[][]; // ����i�ڷ���j��ִ����Ҫ���ѵ�ʱ��
	// ȫ�ι���
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
		int i,j;
		// ��ʼ���洢task-cpu��map
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		// ��ʼ��map�洢result-time��
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		//����DFS���arr��������
		Dfs dfs = new Dfs(num_tasks,num_servers,cost,adj);
		arr = dfs.getDfs();
		// ��̬�洢ĳ����������ʹ�õ�ʱ��
		int curr_state[] = new int[num_servers + 1];
		// ��̬�洢ĳ�����ʵ�����ʱ��AFS
		int finish[] = new int[num_tasks + 1];
		// �������з����ƽ������,�Լ�����˵ķ���
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
		
		// Spend�����洢��ǰ����,spend_pre�����洢δ���������ƽ������
		double spend = 0.0;
		double spend_pre = avg * arr.length;
		
		// SAB Spare Application Budget ��ʾԤ���ʣ��Ԥ��
		double SAB;
		// CTB Current Task Budget ��ʾ��ǰ��������Ļ���
		double CTB;
		// AF Adjustment Factor AF��һ��ֵ�����������ǵ���SAB��CTB��Ӱ��
		double AF;
		// ��ʼ�����������һһ��Ӧ
		for (i = 0; i < num_tasks; i++) {
			int id = arr[i].getId(), min = Integer.MAX_VALUE, min_j = -1, max = 0;
			SAB = Buget - spend - spend_pre;
			if (SAB >= 0) {
				AF = avg / spend_pre;

			} else {
				AF = 0;
			}
			CTB = avg + SAB * AF;
			//System.out.println("\n"+CTB+" "+SAB+" "+AF);
			// ��������ɸѡ�����ϻ���Ҫ��ķ���S
			List<Cpu> S = new ArrayList<Cpu>();
			for (Cpu c : listc) {
				if (c.getSpend() <= CTB) {
					S.add(c);
				}
			}
			// ���SAB��Ԥ��ʣ��Ԥ��>=0,��ѡ�����ķ��񣬷���ѡ������˵�
			if (SAB >= 0) {

				// �����õ��������翪ʼʱ�䣬�����и��ڵ��AFT���Ӧ�ߵ�cost�����ֵ
				for (j = 0; j < adj_par[id].size(); j++) {
					Edge e = adj_par[id].get(j);
					if (e.getSize() + finish[e.getTo()] > max) {
						max = (int) e.getSize() + finish[e.getTo()];
					}
				}
				// ����j������ʹ�õ�ʱ����������翪ʼʱ������ֵ���ϱ������ڱ�����Ļ��ѣ�������õ���Сֵ
				for (Cpu c:S) {
					if (Math.max(curr_state[c.getId()], max) + cost[id][c.getId()] < min) {
						min = Math.max(curr_state[c.getId()], max) + cost[id][c.getId()];
						min_j = c.getId();
					}
				}
			} else {
				// �����õ��������翪ʼʱ�䣬�����и��ڵ��AFT���Ӧ�ߵ�cost�����ֵ
				for (j = 0; j < adj_par[id].size(); j++) {
					Edge e = adj_par[id].get(j);
					if (e.getSize() + finish[e.getTo()] > max) {
						max = (int) e.getSize() + finish[e.getTo()];
					}
				}
				//���㽫task��������˷�����ʱ��ʵ�����ʱ��
				min = Math.max(curr_state[C_min.getId()], max) + cost[id][C_min.getId()];
				min_j = C_min.getId();
				result.put(id, min_j);
				

			}
			//����spend��spend_pre�����������Map
			spend += listc.get(min_j - 1).getSpend();
			spend_pre -= avg;
			curr_state[min_j] = min;
			finish[id] = curr_state[min_j];
			System.out.println();
			System.out.printf("---- Adding Node %d to server %d at time %d ----", id, min_j, min - cost[id][min_j]);

			result.put(id, min_j);
			sche.put(result, (Integer) (min - cost[id][min_j]));

		}
		//��ӡ����
		int Max = 0;
		for (i = 0; i < curr_state.length; i++)
			if (curr_state[i] > Max)
				Max = curr_state[i];

		System.out.printf("\n   Total time taken : %d,total spend : %.2f ", Max, spend);
		return sche;
	}

	

	
}
