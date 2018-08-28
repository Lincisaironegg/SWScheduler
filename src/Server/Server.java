package Server;

import java.io.File;
import java.util.ArrayList;


/**
 * server类 bw:、带宽，单位MBps serv_num：cpu数量 listc:cpu集合
 */
public class Server {
	private int bw;// 固定带宽
	private int ser_num;
	private ArrayList<Cpu> listc;

	public Server(int bw, int ser_num, ArrayList<Cpu> listc) {
		super();
		this.bw = bw;
		this.ser_num = ser_num;
		this.listc = listc;
	}

	public int getSer_num() {
		return ser_num;
	}

	public void setSer_num(int ser_num) {
		this.ser_num = ser_num;
	}

	public Server() {
		super();
		this.bw = 1000;
		this.ser_num = 4;
		this.listc = new ArrayList<Cpu>();
		listc.add(new Cpu(1, 1000));
		listc.add(new Cpu(2, 1200));
		listc.add(new Cpu(3, 1400));
		listc.add(new Cpu(4, 900));

	}

	public int getBw() {
		return bw;
	}

	public void setBw(int bw) {
		this.bw = bw;
	}

	public ArrayList<Cpu> getListc() {
		return listc;
	}

	public void setListc(ArrayList<Cpu> listc) {
		this.listc = listc;
	}

	@Override
	public String toString() {
		return "Server [bw=" + bw + ", listc=" + listc + "]";
	}
	public static Server serverReader(File filename) {
		ServerParser sp = new ServerParser(filename);
		int b = sp.getBw();
		int n = sp.getCpuNum();
		ArrayList<Cpu> c = new ArrayList<Cpu>();
		c = sp.getCpuList();
		Server s = new Server(b,n,c);
		return s;
		
	}

}
