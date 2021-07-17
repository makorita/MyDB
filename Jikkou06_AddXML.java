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

public class Jikkou06_AddXML{
	/*
	XMLDBを生成。基本は置換。
	*/
	public static void main(String args[]) throws Exception{
		String inputXML="MyDB.xml";
		/* 削除検討中
		String mode="Add";
		if(args.length==1 && args[0].equals("replace"))mode="Replace";
		*/
		
		//クリップボードの読み込み
		String clipBoardStr=null;
		Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		{
			Transferable object = clipboard.getContents(null);
			clipBoardStr = (String)object.getTransferData(DataFlavor.stringFlavor);
		}
		
		//doc,rootの生成
		Document doc =null;
		Element rootElement=null;
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			if(new File(inputXML).exists()){
				doc=docBuilder.parse(new File(inputXML));
				rootElement=doc.getDocumentElement();
			}else{
				doc=docBuilder.newDocument();
				rootElement = doc.createElement("data");
				doc.appendChild(rootElement);
			}
		}
		
		//インプットパスの読み込み
		LinkedList<LinkedList<String>> inputList=new LinkedList<LinkedList<String>>();
		{
			String word[]=clipBoardStr.split("\n");
			for(String tmpStr:word){
				LinkedList<String> curList=new LinkedList<String>();
				String[] word2=tmpStr.split("\t");
				for(String curStr:word2){
					if(curStr.matches(";.+"))break;
					if(curStr.matches("//.+"))break;
					if(curStr.length()==0)continue;
					curList.add(curStr);
					//System.out.println(curStr);
				}
				
				if(curList.size()>=2)inputList.add(curList);
			}
		}
		
		//xmlへのデータ登録
		{
			for(LinkedList<String> curList:inputList){
				Element parentElement=rootElement;
				for(int i=0;i<curList.size();i++){
					String tmpStr=curList.get(i);
					tmpStr=normalize(tmpStr);	//使用できない文字を置換する
					//System.out.println(tmpStr);
					
					if(i==curList.size()-1){	//valueの生成
						parentElement.setTextContent(tmpStr);
						parentElement.setAttribute("type","value");
						//System.out.println("text:"+tmpStr);
						
					}else if(tmpStr.matches(".*:list")){
						tmpStr=tmpStr.replaceAll(":list","");
						NodeList childList=parentElement.getElementsByTagName(tmpStr);
						int maxIndex=0;
						for(int j = 0; j < childList.getLength(); j++) {
							Node node = childList.item(j);
							Element childElement = (Element)node;
							if(childElement.getAttribute("id").length()>0){
								int curInt=Integer.parseInt(childElement.getAttribute("id"));
								if(curInt>maxIndex)maxIndex=curInt;
							}
						}
						
						Element curElement=doc.createElement(tmpStr);
						parentElement.appendChild(curElement);
						parentElement=curElement;
						curElement.setAttribute("id",String.valueOf(maxIndex+1));
					}else{
						NodeList childList=parentElement.getElementsByTagName(tmpStr);
						if(childList.getLength()>0){
							Node node = childList.item(0);
							Element childElement = (Element)node;
							parentElement=childElement;
						}else{
							Element curElement=doc.createElement(tmpStr);
							parentElement.appendChild(curElement);
							parentElement=curElement;
						}
					}
				}
			}
		}
		
		//XMLファイルの保存
		{
			TransformerFactory tfFactory = TransformerFactory.newInstance();
			Transformer tf = tfFactory.newTransformer();

			tf.setOutputProperty("indent", "yes");
			tf.setOutputProperty("encoding", "UTF-8");

			tf.transform(new DOMSource(doc), new StreamResult(inputXML));
		}
		
		//バキューム処理
		{
			BufferedReader br = new BufferedReader(new FileReader(inputXML));
			//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "EUC-JP"));
			String line;
			LinkedList<String> contentList=new LinkedList<String>();
			while ((line = br.readLine()) != null) {
				//System.out.println(line);
				contentList.add(line);
			}
			br.close();
			
			PrintWriter wr=new PrintWriter(new FileWriter(inputXML));
			for(String curStr:contentList){
				if(curStr.matches(" *"))continue;
				wr.println(curStr);
			}
			wr.close();
		}
	}
	
	static String normalize(String originalStr){
		String returnStr=originalStr;
		returnStr=returnStr.replace("①","1");
		returnStr=returnStr.replace("②","2");
		returnStr=returnStr.replace("③","3");
		returnStr=returnStr.replace("④","4");
		returnStr=returnStr.replace("⑤","5");
		returnStr=returnStr.replace("⑥","6");
		returnStr=returnStr.replace("⑦","7");
		returnStr=returnStr.replace("⑧","8");
		returnStr=returnStr.replace("⑨","9");
		returnStr=returnStr.replace("⑩","10");
		
		return returnStr;
	}
}