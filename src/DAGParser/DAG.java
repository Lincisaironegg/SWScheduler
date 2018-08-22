package DAGParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;


//�в��˽��Ǳ�xmlParser���������ṹ����ʱӲ���룬��xmlParser���ͺ󣬿ɸ������������DAG
public class DAG {

	private static int task_num;
	private static int edge_num;
	private static double Buget;
	private static int Dl;

	public int getDl() {
		return Dl;
	}

	public void setDl(int dl) {
		Dl = dl;
	}

	public double getBuget() {
		return Buget;
	}

	public void setBuget(double buget) {
		Buget = buget;
	}

	ArrayList<Task> listT;

	public DAG() {
		super();
//		Buget = 1120.0;
//		task_num = 10;
//		ArrayList<Edge> liste1 = new ArrayList<Edge>();
//		ArrayList<Edge> liste2 = new ArrayList<Edge>();
//		ArrayList<Edge> liste3 = new ArrayList<Edge>();
//		ArrayList<Edge> liste4 = new ArrayList<Edge>();
//		ArrayList<Edge> liste5 = new ArrayList<Edge>();
//		ArrayList<Edge> liste6 = new ArrayList<Edge>();
//		ArrayList<Edge> liste7 = new ArrayList<Edge>();
//		ArrayList<Edge> liste8 = new ArrayList<Edge>();
//		ArrayList<Edge> liste9 = new ArrayList<Edge>();
//		ArrayList<Edge> liste10 = new ArrayList<Edge>();
//		liste1.add(new Edge(2, 18000));
//		liste1.add(new Edge(3, 12000));
//		liste1.add(new Edge(4, 9000));
//		liste1.add(new Edge(5, 11000));
//		liste1.add(new Edge(6, 14000));
//		liste1.add(new Edge(8, 19000));
//		liste2.add(new Edge(9, 16000));
//		liste2.add(new Edge(7, 23000));
//		liste3.add(new Edge(8, 27000));
//		liste4.add(new Edge(9, 28000));
//		liste5.add(new Edge(9, 13000));
//		liste6.add(new Edge(8, 15000));
//		liste7.add(new Edge(10, 17000));
//		liste8.add(new Edge(10, 11000));
//		liste9.add(new Edge(10, 13000));

//		listT = new ArrayList<Task>();
//		Task t1 = new Task("0001", 18000, liste1);
//		Task t2 = new Task("0002", 17500, liste2);
//		Task t3 = new Task("0003", 20000, liste3);
//		Task t4 = new Task("0004", 14600, liste4);
//		Task t5 = new Task("0005", 18400, liste5);
//		Task t6 = new Task("0006", 11600, liste6);
//		Task t7 = new Task("0007", 15800, liste7);
//		Task t8 = new Task("0008", 19400, liste8);
//		Task t9 = new Task("0009", 14400, liste9);
//		Task t10 = new Task("0010", 16000, liste10);
//		listT.add(t1);
//		listT.add(t2);
//		listT.add(t3);
//		listT.add(t4);
//		listT.add(t5);
//		listT.add(t6);
//		listT.add(t7);
//		listT.add(t8);
//		listT.add(t9);
//		listT.add(t10);

	}
	//��xml�ļ��ж�ȡDAGͼ�ķ���
	public static DAG dagReader(File filename) {
		
		xmlParser xp = new xmlParser(filename);//ʵ����xmlParser���Խ��ж�ȡ
		
		Buget = xp.getBuget();  //��xml��ȡBuget��Edge_num,task_num,deadline����Ϣ
		edge_num = xp.getEdgenum();
		task_num = xp.getTasknum();
		Dl = xp.getDl();
		
		//����xmlParser��traversalDocumentByIterator()�������õ��ӽڵ�Ԫ�أ�����node��link��relation���㣬���ӹ�ϵ���ߣ�
		Map<String,List<Element>> map = xp.traversalDocumentByIterator();
		ArrayList<Task> listT;  //listT���ڴ洢task
		listT = new ArrayList<Task>();
		//��һ��ѭ��������node��㣬����ӵ�listT
		for (Iterator<Element> iter = map.get("node").iterator(); iter.hasNext();) {
			Element e = iter.next();
			//���ڴ洢�ߵ���Ϣ
			ArrayList<Edge> listE = new ArrayList<Edge>();
			//�ڶ���ѭ�����ڱ���link�����ҳ��ͽڵ����Ӧ�����ӹ�ϵ����ȡ�����Լ��ߵ���Ϣ�洢��listE
			for (Iterator<Element> iter1 = map.get("link").iterator(); iter1.hasNext();) {
				Element c = iter1.next();

				String p = null;//p���ڴ洢�ߴ����������
				//if���ɸѡ��ӵ��Ӧ������
				if ((c.attributeValue("port")).equals(e.attributeValue("name"))) {
					//������ѭ�������ߣ���ɸѡ�����Ӷ�Ӧ�ıߣ�����ȡ����
					for (Iterator<Element> iter2 = map.get("relation").iterator(); iter2
							.hasNext();) {
						Element r = iter2.next();
						//if���ɸѡ�����Ӷ�Ӧ�ı�
						if ((r.attributeValue("name")).equals(c.attributeValue("relation"))) {
							
							for (Iterator<Element> ieson = r.elementIterator(); ieson.hasNext();) {
								
								Element elementSon = (Element) ieson.next();
								if (elementSon.getName().equals("property")) {
									p = elementSon.attributeValue("size");
									//���Դ�ӡ
									//System.out.println(p);
								}
							}

						}
						
	
					}

				}else {//������Ƕ�Ӧ�ľ��ٴ�ѭ�������ұ���linkÿ��ѭ�����ɶԵ���
					iter1.next();
					continue;
				}
				//���ߵ���Ϣ����listE
				listE.add(new Edge(Integer.parseInt(iter1.next().attributeValue("port")),Integer.parseInt(p)));
				//���Դ�ӡ
				//System.out.println(listE.toString());
			}
			//���Դ�ӡ
			//System.out.println("task"+listE.toString());
			listT.add(new Task(e.attributeValue("name"), Integer.parseInt(e.element("property").attributeValue("size")),
					listE));
			
		}
		//����һ��DAGʵ���ύ�������㷨
		DAG dag = new DAG(task_num, edge_num, listT, Buget);
		return dag;

	}

	public DAG(int Task_num, int Edge_num, ArrayList<Task> ListT, double buget) {
		super();
		task_num = Task_num;
		edge_num = Edge_num;
		listT = ListT;
		Buget = buget;
	}

	public ArrayList<Task> getList() {
		return this.listT;

	}

	public int getTask_num() {
		return task_num;
	}

	public void setTask_num(int Task_num) {
		task_num = Task_num;
	}

	public int getEdge_num() {
		return edge_num;
	}

	public void setEdge_num(int Edge_num) {
		edge_num = Edge_num;
	}

	@Override
	public String toString() {
		return "DAG [task_num=" + task_num + ", edge_num=" + edge_num + ", Buget=" + Buget + ", Dl=" + Dl + ", listT="
				+ listT + "]";
	}

}
