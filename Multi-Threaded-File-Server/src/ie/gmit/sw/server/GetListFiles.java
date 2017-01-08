package ie.gmit.sw.server;

import java.io.File;
import java.util.ArrayList;

public class GetListFiles {
	private String directory;
	
	public GetListFiles(String dir){
		this.directory = dir;
		
	}
	
	public ArrayList files(){ 
		//create an arraylist to store all the files for searching
		ArrayList<String> listOfFiles = new ArrayList<String>();
		
		File fileLocation = new File(directory);
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
		//return the arraylist
		return listOfFiles;
		
	}
		
}//GetListFiles
