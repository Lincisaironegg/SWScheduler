package DAGParser;



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
public class xmlParser {
	private static File inputXml;
	
	public xmlParser(File input) {
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
//用于远程读取xml文件
//    public static Document getDocument(URL url) {  
//        Document document = null;  
//        try {  
//            SAXReader saxReader = new SAXReader();  
//            document = saxReader.read(url); // 读取XML文件,获得document对象  
//        } catch (Exception ex) {  
//            ex.printStackTrace();  
//        }  
//        return document;  
    //在document格式中获取根节点
    public static Element getRootElement() {
        return getDocument().getRootElement();
     }
    public double getBuget() {
    	Element root = getRootElement();
    	Attribute bugetAttr = root.attribute("Buget");
    	double Buget = Double.parseDouble(bugetAttr.getValue());
    	return Buget;
    }
    public int getDl() {
    	Element root = getRootElement();
    	Attribute bugetAttr = root.attribute("DeadLine");
    	int Dl = Integer.parseInt(bugetAttr.getValue());
    	return Dl;
    }
    public int getTasknum() {
    	return  traversalDocumentByIterator().get("node").size();
    }
    public int getEdgenum() {
    	return  traversalDocumentByIterator().get("relation").size();
    }
    @SuppressWarnings("rawtypes")
	public Map<String, List<Element>> traversalDocumentByIterator(){
    	Element root = getRootElement();
    	List<Element> nodeList = new ArrayList<Element>();
    	List<Element> relationList = new ArrayList<Element>();
    	List<Element> linkList = new ArrayList<Element>();
    	

		//// 枚举根节点下所有子节点，三种标签node,relation,link
		for (Iterator ie = root.elementIterator(); ie.hasNext();) {
			
            Element element = (Element) ie.next();
            Attribute attribute = null;
            if (element.getName().equals("node")){
            	//System.out.println(element.getName());
            	nodeList.add(element);
            }
            else if(element.getName().equals("relation")){
            	//System.out.println(element.getName());
            	relationList.add(element);
            }
            else if(element.getName().equals("link")){
            	//System.out.println(element.getName());
            	linkList.add(element);
            }else{
            	System.out.println("标签不满足条件");
            }
            //System.out.println(element.getName());
            
            // 枚举属性
            for (Iterator ia = element.attributeIterator(); ia.hasNext();) {
                attribute = (Attribute) ia.next();      
            }

            for (Iterator ieson = element.elementIterator(); ieson.hasNext();) {
                Element elementSon = (Element) ieson.next();
                if (elementSon.getName().equals("property")){
                	for (Iterator ias = elementSon.attributeIterator(); ias.hasNext();) {
                    attribute = (Attribute) ias.next();                                     
                    }
                }else{
                	System.out.println("标签不满足条件");
                }  
             }             
         }
		
    	//将三个列表存入map中
		Map<String, List<Element>> map = new HashMap<String,List<Element>>();
		map.put("node", nodeList);
		map.put("relation", relationList);
		map.put("link", linkList);
		
    	return map;
    }
    

}
