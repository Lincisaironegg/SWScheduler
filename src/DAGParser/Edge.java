package DAGParser;

/**
 * �� �� to:��ָ����ӽڵ� size:�������ݴ�С����λMB
 *
 */

public class Edge {
	private int to;
	private long size;

	public Edge(int x, long y) {
		to = x;
		size = y;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "Edge [to=" + to + ", size=" + size + "]";
	}
	

}
