package ie.gmit.sw.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Runner {
	//Variables
	private static ContextParser ctx;
	private static Socket requestSocket;
	private static ObjectOutputStream out;
 	private static ObjectInputStream in;
 

	public static void main(String[] args) {
		int response = 0;
		Scanner scanner = new Scanner(System.in);
		//Create a socket Connection
		Socket s = null;
		
		do{
			System.out.println("\n==================Menu==================");
			System.out.println("1. Connect to Server");
			System.out.println("2. Print File Listing");
			System.out.println("3. Download File");
			System.out.println("4. Quit");
		
			System.out.println("Type Option [1-4] (Please Connect To The Server first before using option 2 or 3)>");
			response = scanner.nextInt();
			
			if(response == 1){
				if(s != null){
					System.out.println("You Are Already Connected to the Server!");
				}
				else{
					connect();
				}
			}
			else if( response == 2){
				if(s != null){
					System.out.println("Open a Connection To The Server First!");
				}
				else{
					printFileListing();
				}
			}
			else if(response == 3){
				downloadFile();
			}
			else if(response == 4){
				if(s != null){
					System.out.println("No Connection Open - Closing Program");
				}
				else{
					closeConnection();
				}
			}
		}while(response != 4);
		
		scanner.close();
	}

	//Connect to the server
	public static void connect() { 
		
		try { //Attempt the following. If something goes wrong, the flow jumps down to catch()
			requestSocket = new Socket("localhost", 7777); //Connect to the server
	
			
			System.out.println("Successfully Connected To The Server!");			
		
		} catch (Exception e) { //Deal with the error here. A try/catch stops a programme crashing on error  
			System.out.println("Error: " + e.getMessage());
		}//End of try /catch

	}//End of run(). The thread will now die...sob..sob...;)
	
	//Print File Listing
	public static void printFileListing(){
		try {
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			String command = "2";
			out.writeObject(command);
			out.flush();
			
			in = new ObjectInputStream(requestSocket.getInputStream());
			try {
				String response = (String) in.readObject();
				System.out.println(response);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} //Deserialise
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	
	//Downlaod File
	private static void downloadFile() {
		
		
	}
	
	//Close Connection
	public static void closeConnection() {
		try {
			requestSocket.close();
			System.out.println("Conection Closed - Program Closing!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
}//Runner
