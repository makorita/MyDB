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

public class A_Clipboard2XML{
	/*
	XMLDBを生成。基本は置換。
	*/
	public static void main(String args[]) throws Exception{
		//クリップボードの読み込み
		String clipBoardStr=null;
		Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		{
			Transferable object = clipboard.getContents(null);
			clipBoardStr = (String)object.getTransferData(DataFlavor.stringFlavor);
		}
		
		//XMLDBの生成
		MyXMLDB myDB =null;
		{
			myDB=new MyXMLDB();
			myDB.loadXML();
		}
		
		//データの読み込み
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
				
				if(curList.size()>=2)myDB.mergeList(curList);
			}
			
			myDB.saveXML();
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