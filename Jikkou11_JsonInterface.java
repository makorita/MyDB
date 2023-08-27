import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Jikkou11_JsonInterface{
	public static void main(String[] args) throws Exception{
		MyJsonDB jdb=new MyJsonDB();
		jdb.load("../ConfigDB.json");
		
		//ヘッダセット作成
		HashSet<String> headerSet=new HashSet<String>();
		{
			headerSet.add("shutdown");
			headerSet.add("description");
			headerSet.add("speed");
			headerSet.add("duplex");
			headerSet.add("IP");
			headerSet.add("Mask");
			headerSet.add("Secondary IP1");
			headerSet.add("Secondary Mask1");
			headerSet.add("Secondary IP2");
			headerSet.add("Secondary Mask2");
			headerSet.add("standbyIP1");
			headerSet.add("standbyIP2");
			headerSet.add("standbyGroup1");
			headerSet.add("standbyGroup2");
			headerSet.add("access group in 番号");
			headerSet.add("access group out 番号");
			headerSet.add("ip nat");
			headerSet.add("switchportMode");
			headerSet.add("accessVlan");
			headerSet.add("allowedVlan");
			headerSet.add("channelGroup");
			headerSet.add("channelMode");
		}
		
		File rootDir=new File("../10_interface");
		String[] fileList=rootDir.list();
		for(String curFile:fileList){
			String hostname=curFile.replace("_Interface.csv","");
			
			LinkedList<String> pathList=new LinkedList<String>();
			pathList.add(hostname);
			pathList.add("interface");
			jdb.addCSV(pathList,"../10_interface/"+curFile,"Shift-JIS","インターフェース名",headerSet);
		}
		
		jdb.save("../ConfigDB.json");
	}
}