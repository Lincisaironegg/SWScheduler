package DAGParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;


//尚不了解那边xmlParser组件的输出结构，暂时硬编码，等xmlParser成型后，可根据其输出定义DAG
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
	//从xml文件中读取DAG图的方法
	public static DAG dagReader(File filename) {
		
		xmlParser xp = new xmlParser(filename);//实例化xmlParser类以进行读取
		
		Buget = xp.getBuget();  //从xml读取Buget，Edge_num,task_num,deadline的信息
		edge_num = xp.getEdgenum();
		task_num = xp.getTasknum();
		Dl = xp.getDl();
		
		//调用xmlParser的traversalDocumentByIterator()方法来得到子节点元素，包括node，link，relation（点，连接关系，边）
		Map<String,List<Element>> map = xp.traversalDocumentByIterator();
		ArrayList<Task> listT;  //listT用于存储task
		listT = new ArrayList<Task>();
		//第一层循环遍历了node结点，并添加到listT
		for (Iterator<Element> iter = map.get("node").iterator(); iter.hasNext();) {
			Element e = iter.next();
			//用于存储边的信息
			ArrayList<Edge> listE = new ArrayList<Edge>();
			//第二次循环用于遍历link，以找出和节点对相应的连接关系，获取连接以及边的信息存储进listE
			for (Iterator<Element> iter1 = map.get("link").iterator(); iter1.hasNext();) {
				Element c = iter1.next();

				String p = null;//p用于存储边传输的数据量
				//if语句筛选与加点对应的连接
				if ((c.attributeValue("port")).equals(e.attributeValue("name"))) {
					//第三层循环遍历边，以筛选与连接对应的边，并获取属性
					for (Iterator<Element> iter2 = map.get("relation").iterator(); iter2
							.hasNext();) {
						Element r = iter2.next();
						//if语句筛选与连接对应的边
						if ((r.attributeValue("name")).equals(c.attributeValue("relation"))) {
							
							for (Iterator<Element> ieson = r.elementIterator(); ieson.hasNext();) {
								
								Element elementSon = (Element) ieson.next();
								if (elementSon.getName().equals("property")) {
									p = elementSon.attributeValue("size");
									//测试打印
									//System.out.println(p);
								}
							}

						}
						
	
					}

				}else {//如果不是对应的就再次循环，并且保持link每次循环都成对迭代
					iter1.next();
					continue;
				}
				//将边的信息存入listE
				listE.add(new Edge(Integer.parseInt(iter1.next().attributeValue("port")),Integer.parseInt(p)));
				//测试打印
				//System.out.println(listE.toString());
			}
			//测试打印
			//System.out.println("task"+listE.toString());
			listT.add(new Task(e.attributeValue("name"), Integer.parseInt(e.element("property").attributeValue("size")),
					listE));
			
		}
		//返回一个DAG实例提交给调度算法
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
