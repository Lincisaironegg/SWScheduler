package Server;

/**
 * cpu�� Mips:cpu������������λmips��������ָ��ÿ�� id:cpu��ʶ
 * spend:���ѣ����������������
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
