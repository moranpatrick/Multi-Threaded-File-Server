package ie.gmit.sw.server;

import java.io.File;
import java.util.ArrayList;

public class GetListFiles {
	
	public static ArrayList files(){ 
		
		ArrayList<String> listOfFiles = new ArrayList<String>();
		
		File fileLocation = new File("C:/Users/Patrick/git/Multi-Threaded-File-Server/Resources/");
		//Return an array of file namees in the resouces folder
		File[] files = fileLocation.listFiles();
		
		//Add the file names to the array list and return it
		for(int i = 0; i < files.length; i++){
			//Test weather its an actual file
			if(files[i].isFile()){
				String fileName = files[i].getName();
				//add the filename to the arraylist
				listOfFiles.add(fileName);
				
			}
		}
		return listOfFiles;
		
	}
		
	
}//GetListFiles
