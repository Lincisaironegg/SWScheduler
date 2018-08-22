package Server;

/**
 * cpu类 Mips:cpu计算能力，单位mips，百万条指令每秒 id:cpu标识
 * spend:花费，与计算能力成正比
 */
public class Cpu {
	
	private int Mips;
	private int id;
	private double spend;

	public double getSpend() {
		return spend;
	}

	public void setSpend(int spend) {
		this.spend = spend;
	}

	public Cpu(int id, int mips) {
		super();
		this.id = id;
		this.Mips = mips;
		this.spend = mips * 0.1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Cpu() {
		super();
		Mips = 1000;
	}

	public int getMips() {
		return Mips;
	}

	public void setMips(int mips) {
		Mips = mips;
	}

	@Override
	public String toString() {
		return "Cpu [Mips=" + Mips + ", id=" + id + "]";
	}

}
