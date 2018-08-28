package util;



public class task_node implements Comparable<task_node> {
	int id;
	float dlp;
	int subDl;
	


	

	public task_node() {
		super();
		// TODO Auto-generated constructor stub
	}


	public int getSubDl() {
		return subDl;
	}

	public void setSubDl(int subDl) {
		this.subDl = subDl;
	}

	public task_node(int x) {
		id = x;
		dlp = 0;
	}
	

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public float getDlp() {
		return dlp;
	}


	public void setDlp(float dlp) {
		this.dlp = dlp;
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
