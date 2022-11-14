import java.io.*;
import java.util.*;

import org.apache.poi.*;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.*;
import org.apache.poi.xssf.usermodel.*;

public class MyExcelDB{
	Workbook wb;
	
	public MyExcelDB() throws Exception{
	}
	
	public void close() throws Exception{
		wb.close();
	}
	
	public void loadExcel() throws Exception{
		wb = WorkbookFactory.create(new FileInputStream("MyDB.xlsx"));
	}
	
	public void saveExcel() throws Exception{
		//ファイル保存
		FileOutputStream out = new FileOutputStream("MyDB.xlsx");
		
		wb.write(out);
		
		out.close();
	}
	
	public void loadData(LinkedList<LinkedList<String>> dataList){
		Sheet sheet = getNewSheet();
		
		int rowIndex=0;
		for(LinkedList<String> curList:dataList){
			Row row=sheet.createRow(rowIndex++);
			for(int cellIndex=0;cellIndex<curList.size();cellIndex++){
				Cell cell=row.createCell(cellIndex);
				cell.setCellValue(curList.get(cellIndex));
			}
		}
	}
	
	public Sheet getNewSheet(){
		int maxNum=0;
		for(int sheetIndex=0;sheetIndex<wb.getNumberOfSheets();sheetIndex++){
			Sheet curSheet=wb.getSheetAt(sheetIndex);
			String curSheetName=curSheet.getSheetName();
			if(!curSheetName.matches("data\\d{2}"))continue;
			
			curSheetName=curSheetName.replace("data","");
			int curNum=Integer.parseInt(curSheetName);
			if(curNum>maxNum)maxNum=curNum;
		}
		
		String newSheetName="data"+String.format("%02d", maxNum+1);
		Sheet sheet = wb.createSheet(newSheetName);
		return sheet;
	}
}
