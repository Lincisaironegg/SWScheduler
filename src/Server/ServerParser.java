package Server;





import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
@SuppressWarnings("unused")

public class ServerParser {
	private static File inputXml;
	
	public ServerParser(File input) {
		super();
		inputXml = input;
	}
	 //读取XML文件，转换数据格式，获得document对象
    public static Document getDocument() {
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(inputXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
     }

    //在document格式中获取根节点
    public static Element getRootElement() {
        return getDocument().getRootElement();
     }

    
    public int getCpuNum() {
    	int i = 0;
    	for(Iterator<Element> ic = traversalDocumentByIterator().get("CPU").elementIterator();ic.hasNext();) {
    		Element cpu = ic.next();
    		if(cpu.getName().equals("cgsp")) {
    			i++;
    		}
    	}
    	return i;
    }
    public int getBw() {
    	int Bw = 0;
    	Bw = Integer.parseInt(traversalDocumentByIterator().get("Network").attributeValue("bw"));
    	return Bw;
    }
    public ArrayList<Cpu> getCpuList(){
    	ArrayList<Cpu> listc = new ArrayList<Cpu>();
    	for(Iterator<Element> ic = traversalDocumentByIterator().get("CPU").elementIterator();ic.hasNext();) {
    		Element cpu = ic.next();
    		if(cpu.getName().equals("cgsp")){
    			Cpu c = new Cpu(Integer.parseInt(cpu.attributeValue("core"))+1,Integer.parseInt(cpu.attributeValue("mips")));
    			listc.add(c);
    		}
    	}
    	return listc;
    }
    @SuppressWarnings("rawtypes")
	public Map<String, Element> traversalDocumentByIterator(){
    	Element root = getRootElement();   	
    	Map<String, Element> map = new HashMap<String,Element>();
    	

		// 枚举根节点下所有子节点，三种标签CPU,Memory,Network
    	
    	for (Iterator ie = root.elementIterator(); ie.hasNext();) {
            Element element = (Element) ie.next();
            Attribute attribute = null;
            if (element.getName().equals("CPU")){
            	//System.out.println(element.getName());
            	map.put("CPU", element);
            }
            else if(element.getName().equals("Memory")){
            	//System.out.println(element.getName());
            	map.put("Memory",element);
            }
            else if(element.getName().equals("Network")){
            	//System.out.println(element.getName());
            	map.put("Network",element);
            }else{
            	System.out.println("标签不满足条件");
            }
            //System.out.println(element.getName());
            
            
         }
		
    	//将三个列表存入map中

    	return map;
    }
    

}

