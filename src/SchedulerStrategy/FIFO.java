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
		FIFO o = new FIFO(adj, par, nT, nS, cost, null, listc);
		o.run();
	}

	// DLP�����
	ArrayList<Cpu> listc;
	int num_tasks, num_servers;
	List<Edge> adj[];
	List<Edge> adj_par[];
	int task_sz[]; // ����Ĵ�С����MB��GBΪ��λ����ִ��������Ҫ�Ŀռ�
	int cost[][]; // ����i�ڷ���j��ִ����Ҫ���ѵ�ʱ��
	// ȫ�ι���

	private FIFO(List<Edge> adj[], List<Edge> p[], int nt, int ns, int c[][], int tsz[], ArrayList<Cpu> listc) {
		this.adj = adj.clone();
		adj_par = p;
		num_tasks = nt;
		num_servers = ns;
		cost = c;
		task_sz = tsz;
		this.listc = listc;

	}

	// ��adj[]��id��DLPֵ��װ��task_node�����������

	int max_par[];
	int spend = 0;

	void run() {
		int i;
		int time = 0;
		int[] a = new int[20];// ���ڱ�Ǹýڵ��Ƿ���䣬1Ϊ�ѷ��䣬0Ϊδ����

		for (i = 1; i < num_tasks; i++) {
			int min_j = 1 + (int) (Math.random() * 4);// �������һ��������

			if (a[i] != 1) {

				a[i] = 1;

				System.out.printf("---- Adding Node %d to server %d at time %d ----\n", i, min_j, time);
				spend += listc.get(min_j - 1).getSpend();
				time += cost[i][min_j];
				int sz;
				sz = adj[i].size();
				for (int e = 0; e < sz; e++) {
					Edge ed = adj[i].get(e);
					if (a[ed.getTo()] != 1) {// ����ýڵ���ӽڵ�

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