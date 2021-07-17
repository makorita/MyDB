import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.datatransfer.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.apache.poi.*;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Jikkou08_SearchDB{
	/*
	選択文字を含むデータを検索する
	*/
	static LinkedList<Node> nodeList;
	
	public static void main(String args[]) throws Exception{
		//クリップボードの読み込み
		String clipBoardStr=null;
		Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		{
			Transferable object = clipboard.getContents(null);
			clipBoardStr = (String)object.getTransferData(DataFlavor.stringFlavor);
		}
		
		//インプット情報の読み込み
		String[] searchList=clipBoardStr.split("\n");
		StringBuilder editStr = new StringBuilder();
		
		//ExcelDBの検索
		{
			Workbook wb = WorkbookFactory.create(new FileInputStream("MyDB.xlsx"));
			for(int sheetIndex=0;sheetIndex<wb.getNumberOfSheets();sheetIndex++){
				Sheet sheet=wb.getSheetAt(sheetIndex);
				Row headerRow=sheet.getRow(0);
				LinkedList<String> headerList=new LinkedList<String>();
				for(int cellIndex=0;cellIndex<headerRow.getLastCellNum();cellIndex++){
					Cell headerCell=headerRow.getCell(cellIndex);
					
					String headerCellValue=null;
					if(headerCell.getCellType()==CellType.STRING)headerCellValue=headerCell.getStringCellValue();
					else if(headerCell.getCellType()==CellType.NUMERIC)headerCellValue=String.valueOf((int)headerCell.getNumericCellValue());
					else headerCellValue="";
					
					headerList.add(headerCellValue);
				}
					
				LABEL:for(int rowIndex=1;rowIndex<=sheet.getLastRowNum();rowIndex++){	//rowの最大値は最大index
					Row row=sheet.getRow(rowIndex);
					if(row==null)continue;
					
					for(int cellIndex=0;cellIndex<row.getLastCellNum();cellIndex++){
						Cell cell=row.getCell(cellIndex);
						String cellValue=null;
						if(cell.getCellType()==CellType.STRING)cellValue=cell.getStringCellValue();
						else if(cell.getCellType()==CellType.NUMERIC)cellValue=String.valueOf((int)cell.getNumericCellValue());
						else cellValue="";
						
						for(String curStr:searchList){
							if(!cellValue.matches("[\\s\\S]*"+curStr+"[\\s\\S]*"))continue;
							
							StringBuilder tmpEditStr = new StringBuilder();
							for(int tmpCellIndex=0;tmpCellIndex<row.getLastCellNum();tmpCellIndex++){
								Cell tmpCell=row.getCell(tmpCellIndex);
								String tmpCellValue=null;
								if(tmpCell.getCellType()==CellType.STRING)tmpCellValue=tmpCell.getStringCellValue();
								else if(tmpCell.getCellType()==CellType.NUMERIC)tmpCellValue=String.valueOf((int)tmpCell.getNumericCellValue());
								else tmpCellValue="";
								
								tmpEditStr.append(","+headerList.get(tmpCellIndex)+":"+tmpCellValue);
							}
							
							editStr.append(tmpEditStr.toString().replaceFirst(",","")+"\n");
							continue LABEL;
						}
					}
				}
			}			
			wb.close();
			
			//System.out.println(editStr);
		}
		
		{
			nodeList=new LinkedList<Node>();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File("MyDB.xml"));
			Element rootElement=doc.getDocumentElement();
			recursiveCheck(rootElement);
			//System.out.println(nodeList.size());
			
			for(Node curNode:nodeList){
				for(String searchStr:searchList){
					if(!curNode.getTextContent().matches("[\\s\\S]*"+searchStr+"[\\s\\S]*"))continue;
					
					//System.out.println(curNode.getTextContent()+","+searchStr);
					StringBuffer pathStr=new StringBuffer();
					recursivePathGet(pathStr,curNode);
					//System.out.println(pathStr);
					editStr.append(pathStr+":"+curNode.getTextContent()+"\n");
				}
			}
			
			//System.out.println(editStr);
		}
		
		//クリップボードのセット
		{
			StringSelection selection = new StringSelection(editStr.toString());
			clipboard.setContents(selection, null);
		}
	}
	
	public static void recursivePathGet(StringBuffer pathStr,Node curNode){
		Element curElement=(Element)curNode;
		//System.out.println(curElement.getTagName());
		pathStr.insert(0,"\\"+curElement.getTagName());
		
		Node parentNode=curNode.getParentNode();
		Element parentElement=(Element)parentNode;
		if(parentElement.getTagName().equals("data"))return;
		else recursivePathGet(pathStr,parentNode);
	}
	
	public static void recursiveCheck(Node curNode){
		NodeList childList = curNode.getChildNodes(); 
		for(int i = 0; i < childList.getLength(); i++) {
			if(childList.item(i).getNodeType() != Node.ELEMENT_NODE)continue;
			Element chileElement = (Element)childList.item(i);
			if(chileElement.getAttribute("type").equals("value"))nodeList.add(chileElement);
			else recursiveCheck(chileElement);
		}
	}
}
