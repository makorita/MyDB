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

import org.apache.poi.*;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Jikkou07_AddRDB{
	/*
	ExcelDBを生成。基本は置換。
	*/
	public static void main(String args[]) throws Exception{
		//クリップボードの読み込み
		String clipBoardStr=null;
		Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		{
			Transferable object = clipboard.getContents(null);
			clipBoardStr = (String)object.getTransferData(DataFlavor.stringFlavor);
		}
		
		//インプット情報の読み込み
		LinkedList<String> headerList=new LinkedList<String>();
		TreeMap<String,LinkedList<String>> dataMap=new TreeMap<String,LinkedList<String>>();
		{
			boolean headerFlag=false;
			
			String word[]=clipBoardStr.split("\n");
			for(String tmpStr:word){
				LinkedList<String> curList=new LinkedList<String>();
				String[] word2=tmpStr.split("\t");
				for(String curStr:word2){
					if(curStr.matches(";.+"))break;
					if(curStr.matches("//.+"))break;
					
					if(!headerFlag)headerList.add(curStr);
					else curList.add(curStr);
					//System.out.println(curStr);
				}
				if(headerList.size()>0)headerFlag=true;
				if(curList.size()==headerList.size())dataMap.put(curList.get(0),curList);
			}
		}
		
		Workbook dbWb;
		{
			if(new File("MyDB.xlsx").exists())dbWb = WorkbookFactory.create(new FileInputStream("MyDB.xlsx"));
			else dbWb=new XSSFWorkbook();
			//System.out.println(dbWb.getNumberOfSheets());
		}
		
		Sheet oldSheet=null;
		String oldName=null;
		{
			LABEL:for(int sheetIndex=0;sheetIndex<dbWb.getNumberOfSheets();sheetIndex++){
				Sheet sheet=dbWb.getSheetAt(sheetIndex);
				Row row=sheet.getRow(0);
				if(row==null)continue;
				if(row.getLastCellNum()!=headerList.size())continue;
				
				for(int cellIndex=0;cellIndex<row.getLastCellNum();cellIndex++){
					Cell cell=row.getCell(cellIndex);
					if(cell==null)continue LABEL;
					String cellValue=null;
					if(cell.getCellType()==CellType.STRING)cellValue=cell.getStringCellValue();
					else if(cell.getCellType()==CellType.NUMERIC)cellValue=String.valueOf((int)cell.getNumericCellValue());
					else continue LABEL;
					
					if(!cellValue.equals(headerList.get(cellIndex)))continue LABEL;
				}
				
				oldSheet=sheet;
				oldName=oldSheet.getSheetName();
				break;
			}
		}
		
		if(oldSheet!=null){
			for(int rowIndex=1;rowIndex<=oldSheet.getLastRowNum();rowIndex++){	//ヘッダ行は除く
				Row row=oldSheet.getRow(rowIndex);
				if(row==null)continue;
				
				LinkedList<String> curList=new LinkedList<String>();
				for(int cellIndex=0;cellIndex<row.getLastCellNum();cellIndex++){
					Cell cell=row.getCell(cellIndex);
					String cellValue=null;
					if(cell.getCellType()==CellType.STRING)cellValue=cell.getStringCellValue();
					else if(cell.getCellType()==CellType.NUMERIC)cellValue=String.valueOf((int)cell.getNumericCellValue());
					else cellValue="-";
					
					curList.add(cellValue);
				}
				
				if(!dataMap.containsKey(curList.get(0)))dataMap.put(curList.get(0),curList);
			}
			
			dbWb.removeSheetAt(dbWb.getSheetIndex(oldSheet));
		}
		
		{
			Sheet sheet=dbWb.createSheet();
			dbWb.setSheetName(dbWb.getSheetIndex(sheet),oldName);
			
			//ヘッダセット
			Row headerRow=sheet.createRow(0);
			for(int i=0;i<headerList.size();i++){
				Cell headerCell=headerRow.createCell(i);
				headerCell.setCellValue(headerList.get(i));
			}
			
			//値のセット
			int rowIndex=1;
			for(LinkedList<String> curList : dataMap.values()){
				Row curRow=sheet.createRow(rowIndex++);
				for(int i=0;i<curList.size();i++){
					Cell curCell=curRow.createCell(i);
					curCell.setCellValue(curList.get(i));
				}
			}
		}
		
		{
			dbWb.write(new FileOutputStream("MyDB.xlsx"));
			dbWb.close();
		}
	}
}
