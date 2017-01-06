package ie.gmit.sw.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Runner {

	public static void main(String[] args) {
		int response = 0;
		Scanner scanner = new Scanner(System.in);
		
		do{
			System.out.println("\n==================Menu==================");
			System.out.println("1. Connect to Server");
			System.out.println("2. Print File Listing");
			System.out.println("3. Download File");
			System.out.println("4. Quit");
		
			System.out.println("Type Option [1-4]>");
			response = scanner.nextInt();
			
			if(response == 1){
				run();
			}
		}while(response != 4);
		
		scanner.close();
	}


	public static void run() { 
		
		try { //Attempt the following. If something goes wrong, the flow jumps down to catch()
			Socket s = new Socket("localhost", 7777); //Connect to the server
			

			
			s.close(); //Tidy up
			
		} catch (Exception e) { //Deal with the error here. A try/catch stops a programme crashing on error  
			System.out.println("Error: " + e.getMessage());
		}//End of try /catch
	}//End of run(). The thread will now die...sob..sob...;)
		
	
}
