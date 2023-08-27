import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.json.JSONObject;

import org.apache.poi.*;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.*;
import org.apache.poi.xssf.usermodel.*;

public class MyJsonDB{
	JSONObject root;
	
	public MyJsonDB(){
		root=new JSONObject();
	}
	
	public void load(String srcPath) throws Exception{
		String tmpStr = Files.readString(Paths.get(srcPath));
		root=new JSONObject(tmpStr);
	}
	
	public void save(String dstPath) throws Exception{
		PrintWriter wr=new PrintWriter(new FileWriter(dstPath));
		wr.write(root.toString(3));
		wr.close();
	}
	
	public JSONObject getJSONObject(List<String> pathList){	//ない場合は作る。
		JSONObject curObj=root;
		for(int i=0;i<pathList.size();i++){
			//子供取得
			JSONObject childObj=null;
			if(!curObj.has(pathList.get(i)))childObj=new JSONObject();
			else{
				Object tmpObj=curObj.get(pathList.get(i));
				if(!(tmpObj instanceof JSONObject)){
					curObj.remove(pathList.get(i));
					childObj=new JSONObject();
				}else{
					childObj=curObj.getJSONObject(pathList.get(i));
				}
			}
			curObj.put(pathList.get(i),childObj);
			curObj=childObj;
		}
		
		return curObj;
	}
	
	public void addStr(List<String> pathList,String key,String value){
		JSONObject curObj=getJSONObject(pathList);
		if(curObj.has(key))curObj.remove(key);
		curObj.put(key,value);
	}
	
	public void addMap(List<String> pathList,String key,Map<String,String> dataMap){	//既存キー削除
		JSONObject curObj=getJSONObject(pathList);
		//System.out.println(curObj.toString(3)+","+key);
		if(curObj.has(key))curObj.remove(key);
		curObj.put(key,new JSONObject(dataMap));
	}
	
	public void addCSV(List<String> pathList,String csvPath,String mojiCode,String idCol,Set<String> headerSet) throws Exception{	//既存キー削除
		HashMap<Integer,String> headerMap=new HashMap<Integer,String>();
		int idColNum=-1;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvPath), mojiCode));
		String line;
		while ((line = br.readLine()) != null) {
			//System.out.println(line);
			
			//ヘッダ情報取得
			if(headerMap.size()==0){
				String[] word=line.split(",");
				for(int i=0;i<word.length;i++){
					if(word[i].equals(idCol))idColNum=i;
					if(headerSet.contains(word[i]))headerMap.put(i,word[i]);
				}
				continue;
			}
			
			HashMap<String,String> dataMap=new HashMap<String,String>();
			String idStr=null;
			String[] word=line.split(",");
			for(int i=0;i<word.length;i++){
				if(i==idColNum)idStr=word[i];
				if(!headerMap.containsKey(i))continue;
				if(word[i]==null)continue;
				if(word[i].length()==0)continue;
				
				dataMap.put(headerMap.get(i),word[i]);
			}
			
			addMap(pathList,idStr,dataMap);
		}
		br.close();
	}
	
	public void addExcel(List<String> pathList,String excelPath,String sheetName,String idCol,Set<String> headerSet) throws Exception{	//Excelマップ追加
		HashMap<Integer,String> headerMap=new HashMap<Integer,String>();
		int idColNum=-1;
		
		Workbook wb = WorkbookFactory.create(new FileInputStream(excelPath));
		Sheet sheet=wb.getSheet(sheetName);
		
		LABEL:for(int rowIndex=0;rowIndex<=sheet.getLastRowNum();rowIndex++){	//rowの最大値は最大index
			Row row=sheet.getRow(rowIndex);
			if(row==null)continue;
			
			HashMap<String,String> dataMap=new HashMap<String,String>();
			String idStr=null;
			for(int cellIndex=0;cellIndex<row.getLastCellNum();cellIndex++){
				Cell cell=row.getCell(cellIndex);
				if(cell==null)continue;
				if(cell.getCellType()==CellType.BLANK)continue;
				
				String cellValue=null;
				if(cell.getCellType()==CellType.NUMERIC)cellValue=String.format("%.2f",cell.getNumericCellValue());
				else if(cell.getCellType()==CellType.STRING)cellValue=cell.getStringCellValue();
				else if(cell.getCellType()==CellType.BOOLEAN){
					if(cell.getBooleanCellValue())cellValue="TRUE";
					else cellValue="FALSE";
				}
				//System.out.println(cellValue);
				
				//ヘッダ情報取得
				if(rowIndex==0){
					if(cellValue.equals(idCol))idColNum=cellIndex;
					if(headerSet.contains(cellValue))headerMap.put(cellIndex,cellValue);
					//System.out.println(cellValue+","+idColNum);
					continue;
				}
				
				if(cellIndex==idColNum)idStr=cellValue;
				if(!headerMap.containsKey(cellIndex))continue;
				//System.out.println(cellIndex+","+idStr+","+cellValue);
				
				dataMap.put(headerMap.get(cellIndex),cellValue);
			}
			
			//System.out.println(idStr);
			if(rowIndex>0)addMap(pathList,idStr,dataMap);
		}
		
		wb.close();
	}
	
	public void loadExcel(List<String> pathList,String excelPath,String sheetName) throws Exception{	//Excelパス追加
		Workbook wb = WorkbookFactory.create(new FileInputStream(excelPath));
		Sheet sheet=wb.getSheet(sheetName);
		
		LABEL:for(int rowIndex=0;rowIndex<=sheet.getLastRowNum();rowIndex++){	//rowの最大値は最大index
			Row row=sheet.getRow(rowIndex);
			if(row==null)continue;
			
			LinkedList<String> tmpPathList=new LinkedList<String>();
			for(int cellIndex=0;cellIndex<row.getLastCellNum();cellIndex++){
				Cell cell=row.getCell(cellIndex);
				if(cell==null)continue;
				if(cell.getCellType()==CellType.BLANK)continue;
				
				String cellValue=null;
				if(cell.getCellType()==CellType.NUMERIC)cellValue=String.format("%.2f",cell.getNumericCellValue());
				else if(cell.getCellType()==CellType.STRING)cellValue=cell.getStringCellValue();
				
				if(cellValue==null)continue;
				if(cellIndex==0 && cellValue.matches(";.*"))continue LABEL;
				if(cellIndex==0 && cellValue.matches("//.*"))continue LABEL;
				
				tmpPathList.add(cellValue);
			}
			
			if(tmpPathList.size()<2)continue;
			LinkedList<String> lastPathList=new LinkedList<String>(pathList);
			for(int i=0;i<tmpPathList.size()-2;i++){
				lastPathList.add(tmpPathList.get(i));
			}
			addStr(lastPathList,tmpPathList.get(tmpPathList.size()-2),tmpPathList.get(tmpPathList.size()-1));
		}
		
		wb.close();
	}
	
	public void showAll(){
		System.out.println(root.toString(3));
	}
}
