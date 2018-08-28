package util;

import java.util.Arrays;
import java.util.List;

import DAGParser.Edge;


public class Dfs {
	static int num_tasks;
	static int num_servers;
	static int[][] cost;
	static List<Edge>[] adj;
	static task_node arr[];
	
	public Dfs(int Num_tasks, int Num_servers, int[][] Cost, List<Edge>[] Adj) {
		super();
		num_tasks = Num_tasks;
		num_servers = Num_servers;
		cost = Cost;
		adj = Adj;
	}
	public task_node[] getDfs() {
		
		arr = new task_node[num_tasks];
		for (int i = 0; i < num_tasks; i++)
			arr[i] = new task_node(i + 1);
		for (int i = 0; i < num_tasks; i++)
			if (arr[i].getDlp() == 0)
				dfs(arr[i].getId());
		Arrays.sort(arr);
		return arr;
		
	}
	public float dfs(int node_id)
	{
		if (arr[node_id - 1].getDlp() != 0)
			return arr[node_id - 1].getDlp();
		// ���ڽ���dlpΪÿ�������ϻ��ѵ�ƽ��ֵ
		if (adj[node_id].size() == 0) {
			float avg = 0;
			for (int j = 1; j <= num_servers; j++) {

				avg += cost[node_id][j];
			}
			arr[node_id - 1].setDlp(avg / num_servers);
			return arr[node_id - 1].getDlp();
		}

		int i, sz = adj[node_id].size();
		float max = -1;
		// �����ڵ��ÿ���ߣ�����������ӽڵ�DLP��ͨ��ýڵ�·��cost֮�͵����ֵ
		for (i = 0; i < sz; i++) {
			Edge e = adj[node_id].get(i);
			max = Math.max(dfs(e.getTo()) + e.getSize(), max);
		}
		arr[node_id - 1].setDlp(max);
		float avg = 0;
		// ����ýڵ���ÿ���������ϵ�ƽ��cost
		for (int j = 1; j <= num_servers; j++)
			avg += cost[node_id][j];

		arr[node_id - 1].setDlp(arr[node_id - 1].getDlp() + (avg / num_servers));
		return arr[node_id - 1].getDlp();
	
	}
}
	