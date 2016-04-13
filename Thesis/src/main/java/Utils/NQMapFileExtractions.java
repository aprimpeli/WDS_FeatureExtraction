package Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class NQMapFileExtractions {

	public static NodeFromNQ extractNodeIDFromNQMapFile (String htmlPath, String nqFileMap) throws IOException{
		

		NodeFromNQ node= new NodeFromNQ();
		String htmlfrag[] = htmlPath.split("\\\\");
		String htmlName = htmlfrag[htmlfrag.length-1];
		String nodeID="";
		String title="";
		String description="";
				
		
		FileInputStream in = new FileInputStream(nqFileMap);
		 BufferedReader br = new BufferedReader(new InputStreamReader(in));
		 String strLine;
	 
		  while((strLine = br.readLine())!= null)
		  {
			  String htmlPageName = strLine.split("\\|\\|\\|\\|")[0];
			  if(htmlPageName.equals(htmlName)){
				  nodeID = strLine.split("\\|\\|\\|\\|")[1].split("\\|\\|")[0];
				  title =strLine.split("\\|\\|\\|\\|")[1].split("\\|\\|")[6].replace("title:","");
				  description =strLine.split("\\|\\|\\|\\|")[1].split("\\|\\|")[7].replace("description:","");

				  break;
			  }
		  }
		  br.close();
		  if (nodeID.equals("")) {
			  System.out.println("Something went wrong. The node ID could not be retrieved from the NQFileMap file: "+nqFileMap);
		  }
		  node.setNodeID(nodeID);
		  node.setTitle(title);
		  node.setDescription(description);

		  return node;
	}
}
