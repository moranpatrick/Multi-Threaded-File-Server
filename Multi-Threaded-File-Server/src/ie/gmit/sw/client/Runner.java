package ie.gmit.sw.client;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Runner {
	//Variables
	private static Context ctx;
	private static Socket requestSocket = null;
	private static ObjectOutputStream out;
 	private static ObjectInputStream in;
 	
	public static void main(String[] args) {
		int response = 0;
		Scanner scanner = new Scanner(System.in);
		
		do{
			System.out.println("\n==================Menu==================");
			System.out.println("1. Connect to Server");
			System.out.println("2. Print File Listing");
			System.out.println("3. Download File");
			System.out.println("4. Quit");
		
			System.out.println("Type Option [1-4] (Please Connect To The Server first before using option 2 or 3)>");
			response = scanner.nextInt();
			
			if(response == 1){
				if (requestSocket != null) {
					if(requestSocket.isConnected()){
						System.out.println("You Are Already Connected to the Server!");
					}
				}
				else{
					connect();
				}
			}
			else if(response == 2){
				if(requestSocket == null){
					System.out.println("Open a Connection To The Server First!");
				}
				else{
					printFileListing();
				}
			}
			else if(response == 3){
				if(requestSocket == null){
					System.out.println("Open a Connection To The Server First!");
				}
				else{
					downloadFile();
				}
			}
			else if(response == 4){
				if(requestSocket == null){
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
		try { 
			ctx = new Context();
			ContextParser par = new ContextParser(ctx);
			requestSocket = new Socket(ctx.getServerHost(), ctx.getServerPort()); //Connect to the server			
			System.out.println("Successfully Connected To The Server!");				
		} catch (Exception e) { 
			System.out.println("Error Connecting: " + e.getMessage());
		}
	}//run
	
	//Print File Listing
	public static void printFileListing(){
		try {
			System.out.println("1");
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			System.out.println("2");
			String command = "2";
			out.writeObject(command);
			System.out.println("3");
			out.flush();
			System.out.println("4");
			
			in = new ObjectInputStream(requestSocket.getInputStream());
			System.out.println("5");
			try {
				String response = (String) in.readObject();
				System.out.println("6");
				System.out.println(response);
				System.out.println("7");
			} catch (ClassNotFoundException e) {
				System.out.println("Error printing file listing: " + e.getMessage());
			}	
			System.out.println("8");
		} catch (IOException e) {
			System.out.println("Error in Print File Listing: " + e.getMessage());
		}		
	}//printFileListing
	
	//Downlaod File
	private static void downloadFile() {
		Scanner scanner = new Scanner(System.in);
		String fileToDownload;
		try {
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			String command = "3";
			out.writeObject(command);
			out.flush();
			
			in = new ObjectInputStream(requestSocket.getInputStream());
			try {
				String response = (String) in.readObject();
				System.out.println(response);
				
				fileToDownload = scanner.nextLine();
				out.writeObject(fileToDownload);
				out.flush();
				
				response = (String) in.readObject();
				System.out.println(response);
				
				if(response.equalsIgnoreCase("y")){
					byte[] mybytearray = new byte[1024];
					FileOutputStream fos = new FileOutputStream(ctx.getDownloadDir() + fileToDownload);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					int bytesRead = in.read(mybytearray, 0, mybytearray.length);
					System.out.println(bytesRead);

					bos.write(mybytearray, 0, bytesRead);		

					bos.close();	
					
				}
				else{
					System.out.println("File Not Found on Server!");
				}

						
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}		
		} catch (IOException e) {
			System.out.println("Error Downloading File: " + e.getMessage());
		}	
	}//downloadFile
	
	//Close Connection
	public static void closeConnection() {
		try {	
			requestSocket.close();
			System.out.println("Conection Closed - Program Closing!");
		} catch (IOException e) {
			System.out.println("Error Closing Connection: " + e.getMessage());
		}		
	}
}//Runner

