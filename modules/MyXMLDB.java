import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.datatransfer.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

public class MyXMLDB{
	Document doc =null;
	Element rootElement=null;
	
	public MyXMLDB(){
	}
	
	public void makeNewDoc() throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		doc = docBuilder.newDocument();
		rootElement = doc.createElement("data");
		doc.appendChild(rootElement);
	}
	
	public void loadXML() throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		doc=docBuilder.parse(new File("MyDB.xml"));
		rootElement=doc.getDocumentElement();
	}
	
	public void saveXML() throws Exception{
		TransformerFactory tfFactory = TransformerFactory.newInstance();
		Transformer tf = tfFactory.newTransformer();

//		tf.setOutputProperty("indent", "yes");
		tf.setOutputProperty("encoding", "UTF-8");

		tf.transform(new DOMSource(doc), new StreamResult("MyDB.xml"));
		
		/*
		//バキューム処理
		BufferedReader br = new BufferedReader(new FileReader(dstFile));
		//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "EUC-JP"));
		String line;
		LinkedList<String> contentList=new LinkedList<String>();
		while ((line = br.readLine()) != null) {
			//System.out.println(line);
			contentList.add(line);
		}
		br.close();
		
		PrintWriter wr=new PrintWriter(new FileWriter(dstFile));
		for(String curStr:contentList){
			if(curStr.matches(" *"))continue;
			wr.println(curStr);
		}
		wr.close();
		*/
	}
	
	public void loadList(LinkedList<String> dataList){
		//エレメントの追加
		Element parentElement=rootElement;
		for(int i=0;i<dataList.size();i++){
			if(i==dataList.size()-1){
				parentElement.setTextContent(dataList.get(i));
				break;
			}
			
			Element curElement=doc.createElement(dataList.get(i));
			parentElement.appendChild(curElement);
			parentElement=curElement;
		}
	}
	
	public void mergeList(LinkedList<String> dataList){
		//エレメントの追加
		Element parentElement=rootElement;
		for(int i=0;i<dataList.size();i++){
			if(i==dataList.size()-1){
				parentElement.setTextContent(dataList.get(i));
				break;
			}
			
			Node curNode=parentElement.getElementsByTagName(dataList.get(i)).item(0);
			Element curElement=null;
			if(curNode==null)curElement=doc.createElement(dataList.get(i));
			else curElement=(Element)curNode;
			parentElement.appendChild(curElement);
			parentElement=curElement;
		}
	}
}
