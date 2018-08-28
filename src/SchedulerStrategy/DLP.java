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

//�󲿷������ܵĴ��룬�Ż���װ�������΢��
public class DLP {

	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String[] args) {
		// ��ʼ��һ��Map���ڽ��յ��Ƚ��
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		// ����Server�࣬��ȡcpu�����Ϣ��bw������λMBps����serv_num��cpu�������Լ����cpu��list
		Server Serv = new Server();
		int Bw = Serv.getBw();
		int nS = Serv.getSer_num();
		ArrayList<Cpu> listc = Serv.getListc();
		// ����DAG�࣬��ȡDAG�����޹�ͼ�������Ϣ������task��id����Сsize����λMI����task
		File file = new File("C:\\Users\\bin\\Desktop\\model.xml");
		DAG dag = DAG.dagReader(file);
		int nT = dag.getTask_num();
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
		DLP o = new DLP(adj, par, nT, nS, cost, null,listc);
		sche = o.run();
	}
	// DLP�����
	ArrayList<Cpu> listc;
	int num_tasks, num_servers;
	List<Edge> adj[];
	List<Edge> adj_par[];
	int task_sz[]; // ����Ĵ�С����MB��GBΪ��λ����ִ��������Ҫ�Ŀռ�
	int cost[][]; // ����i�ڷ���j��ִ����Ҫ���ѵ�ʱ��
	// ȫ�ι���

	private DLP(List<Edge> adj[], List<Edge> p[], int nt, int ns, int c[][], int tsz[],ArrayList<Cpu> listc) {
		this.adj = adj.clone();
		adj_par = p;
		num_tasks = nt;
		num_servers = ns;
		cost = c;
		task_sz = tsz;
		this.listc = listc;

	}

	// ��adj[]��id��DLPֵ��װ��task_node�����������
	task_node arr[];
	int max_par[];
	int spend = 0;
	private Map<Map<Integer, Integer>, Integer> run() {
		// ��ʼ���洢task-cpu��map
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		// ��ʼ��map�洢result-time��
		Map<Map<Integer, Integer>, Integer> sche = new HashMap<Map<Integer, Integer>, Integer>();
		int i, j;
		//����DFS���arr��������
		Dfs dfs = new Dfs(num_tasks,num_servers,cost,adj);
		arr = dfs.getDfs();
		// ��̬�洢ĳ����������ʹ�õ�ʱ��
		int curr_state[] = new int[num_servers + 1];
		// ��̬�洢ĳ�����ʵ�����ʱ��AFS
		int finish[] = new int[num_tasks + 1];
		// ��ʼ�����������һһ��Ӧ
		for (i = 0; i < num_tasks; i++) {
			// System.out.println(arr[i].dlp);
			int id = arr[i].getId(), min = Integer.MAX_VALUE, min_j = -1, max = 0;
			// �����õ��������翪ʼʱ�䣬�����и��ڵ��AFT���Ӧ�ߵ�cost�����ֵ
			for (j = 0; j < adj_par[id].size(); j++) {
				Edge e = adj_par[id].get(j);
				if (e.getSize() + finish[e.getTo()] > max) {
					max = (int) e.getSize() + finish[e.getTo()];
				}
			}
			// ����j������ʹ�õ�ʱ����������翪ʼʱ������ֵ���ϱ������ڱ�����Ļ��ѣ�������õ���Сֵ
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

	// �ݹ�ؼ����ÿ���ڵ��dlpֵ��
	// ���Խ������� dlp=������ÿ�������ϻ��ѵ�ƽ��ֵ+max��e.to.dlp+e.weight)��
	// ������ִ��ʱ��ʸ����



}