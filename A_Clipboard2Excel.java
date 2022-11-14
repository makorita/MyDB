import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.datatransfer.*;

import org.w3c.dom.*;

import org.apache.poi.*;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class A_Clipboard2Excel{
	/*
	ExcelDBを生成。基本は置換。
	*/
	public static void main(String args[]) throws Exception{
		//クリップボードの読み込み
		String clipBoardStr=null;
		{
			Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable object = clipboard.getContents(null);
			clipBoardStr = (String)object.getTransferData(DataFlavor.stringFlavor);
		}
		
		//インプット情報の読み込み
		LinkedList<LinkedList<String>> dataList=new LinkedList<LinkedList<String>>();
		{
			String word[]=clipBoardStr.split("\n");
			for(String tmpStr:word){
				if(tmpStr.matches(";.+"))continue;
				if(tmpStr.matches("//.+"))continue;
				
				LinkedList<String> curList=new LinkedList<String>();
				dataList.add(curList);
				String[] word2=tmpStr.split("\t");
				for(String curStr:word2){
					curList.add(curStr);
				}
			}
		}
		
		//Excel保存
		{
			MyExcelDB myDb=new MyExcelDB();
			myDb.loadExcel();
			myDb.loadData(dataList);
			myDb.saveExcel();
		}
	}
}
