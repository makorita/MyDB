import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.json.JSONObject;

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
		if(curObj.has(key))curObj.remove(key);
		curObj.put(key,new JSONObject(dataMap));
	}
	
	public void addCSV(List<String> pathList,String csvPath,String mojiCode,String idCol,Set<String> headerSet) throws Exception{	//既存キー削除
		TreeMap<Integer,String> headerMap=new TreeMap<Integer,String>();
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
			
			TreeMap<String,String> dataMap=new TreeMap<String,String>();
			String idStr=null;
			String[] word=line.split(",");
			for(int i=0;i<word.length;i++){
				if(i==idColNum)idStr=word[i];
				if(!headerMap.containsKey(i))continue;
				
				dataMap.put(headerMap.get(i),word[i]);
			}
			
			addMap(pathList,idStr,dataMap);
		}
		br.close();
	}
	
	public void showAll(){
		System.out.println(root.toString(3));
	}
}
