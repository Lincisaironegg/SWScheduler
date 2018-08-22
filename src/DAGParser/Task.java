package DAGParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务类 id:任务的唯一标识 size:任务大小，单位MI（Million Instructions）百万条指令 liste:存储边的数组
 */
public class Task {
	private String id;
	private long size;
	private ArrayList<Edge> liste;

	public Task(String id, long size, ArrayList<Edge> liste) {
		super();
		this.id = id;
		this.size = size;
		this.liste = liste;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public List<Edge> getListe() {
		return liste;
	}

	public void setListe(ArrayList<Edge> liste) {
		this.liste = liste;
	}

	public Task() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", size=" + size + ", liste=" + liste + "]";
	}

}
