package ie.gmit.sw.server;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WebServer {
	private ServerSocket ss; //A server socket listens on a port number for incoming requests
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	private volatile boolean keepRunning = true;
	private String directory;
	private int port;
	//String used to stop the blocking queue
	private final String POISON_PILL = "POISON";
	
	//An array blocking queue used for the logging
	BlockingQueue<String> bq = new ArrayBlockingQueue<String>(100);

	//Server takes in two arguments when its started - the port number and the path to files
	private WebServer(String port, String dir){
		this.directory = dir;
		//convert string argument for port to integer
		this.port = Integer.parseInt(port);

		try { 	
			//Start the server socket listening on port enterted on startup
			ss = new ServerSocket(this.port); 
			Thread server = new Thread(new Listener(), "Web Server Listener"); 
			server.setPriority(Thread.MAX_PRIORITY); 
			server.start(); 
			
			//Create new Thread to handle the Blocking Queue
			Thread logger = new Thread(new Logger(), "Logger");
			logger.setPriority(Thread.MAX_PRIORITY); 
			logger.start();
			
			System.out.println("Server started and listening on port " + this.port);	
		} catch (IOException e) { 
			System.out.println("Yikes! Something bad happened..." + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		//The application requires 2 arguments to start. Port number and the path to your files
		if(args.length < 2){
			System.out.println("You must enter a port number and path to your files.");
		}
		else{
			//Two arguments - port num and path to files
			new WebServer(args[0], args[1]);  
		}	
	}//Main
	
	private class Listener implements Runnable{ 
		
		public void run() {
			int counter = 0; 
			while (keepRunning){ 
				try {				
					Socket s = ss.accept(); 
					new Thread(new HTTPRequest(s), "T-" + counter).start();
					counter++; 
				} catch (IOException e) { 
					System.out.println("Error handling incoming request..." + e.getMessage());
				}
			}
		}//run()
	}//End of inner class Listener

	private class HTTPRequest implements Runnable{
		private Socket sock; 
		
		private HTTPRequest(Socket request) { 
			this.sock = request; 
		}
		//main run method to handle each request
        public void run() {           
        	try{ 
            	while(keepRunning){
                   	in = new ObjectInputStream(sock.getInputStream());
                   	out = new ObjectOutputStream(sock.getOutputStream());
	                Object command = in.readObject(); 
	                //Print Files
	            	if(command.toString().equals("2")){
	            		//add to the blocking queue - a listing was requested...time and date ect..
	            		bq.put("INFO Listing requested by " + sock.getInetAddress() + " at " + LocalDateTime.now() + "\n");
	            		
	            		GetListFiles getFiles = new GetListFiles(directory);
	            		String files = getFiles.files().toString();
	            		message = files;
	            		out.writeObject(message);
	            		out.flush();    		         		
	            	}
	            	//Download a file
	            	else if(command.toString().equals("3")){
	            		//add to the blocking queue - a File was requested...time and date ect..
	            		bq.put("INFO File requested by " + sock.getInetAddress() + " at " + LocalDateTime.now() + "\n");
	                    
	            		message = "What file do you wish to download?";
	                	out.writeObject(message);
	                    out.flush();                    
	                    command = in.readObject();
	                    File targetFile = new File(directory + command.toString());
             
	                    //Send a message back weather the file exists or not so it can be handled on the client side
	                    if(targetFile.exists()){   
	                    	message = "y";   	
	                    }
	                    else{
	                    	message = "n";
	                    	//Add a warning to the log file - The File doesn't exist
	                    	bq.put("WARN File requested by " + sock.getInetAddress() + " at " + LocalDateTime.now() + " was not found!\n");
	                    }
	                	out.writeObject(message);
	                    out.flush();
	                    
	                    if(targetFile.exists()){
	                    	//file exists so send it over in byte array
		                    byte[] mybytearray = new byte[(int) targetFile.length()];
		                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(targetFile));
		                    bis.read(mybytearray, 0, mybytearray.length);
		                    
		                    out.write(mybytearray, 0, mybytearray.length);
		                    out.flush();	
	                    }
	            	}               	
            	}//while 
            //Handle Exceptions
            } catch (FileNotFoundException e) {
            	System.out.println("File not Found on Server");
            } catch (EOFException e) {
            	try {
            		//This how I kill the blocking queue
            		//Upon option 4 being selected the string "POISON_PILL" is added to the blocking queue
					bq.put(POISON_PILL);
				} catch (InterruptedException e1) {
					System.out.println("Interupted Thread: " + e1.getMessage());;
				}       	
            } catch (Exception e) {
            	System.out.println("Error processing request from " + sock.getRemoteSocketAddress());
            	System.out.println("Error: " + e.getMessage());
            }        	
        }     
	}//End of inner class HTTPRequest
	
	//Inner class Logger to handle the blocking queue
	private class Logger implements Runnable{
		
		public void run(){
			try{
				File file = new File("Requests.log");
				//Create a new File if it doesn't exist already
				if(!file.exists()){
					file.createNewFile();
			    }
	
		    	while(keepRunning){
		    		try {
				    	String message = bq.take();
				    	if(message.equals(POISON_PILL)){
				    		//break the loop
				    		break;  		
				    	}
				    	else{
					    	FileWriter fw = new FileWriter(file,true);
					    	BufferedWriter bw = new BufferedWriter(fw);
					    	bw.write(bq.take());
							bw.close();
				    	}
					} catch (InterruptedException e) {
						System.out.println("Thread Interutpted: " + e.getMessage());
					} 		
		    	}//while
			} catch (IOException e){
				System.out.println("I/O Exception: " + e.getMessage());
			}			
		}//Run
	}//End of inner class Logger
}//WebServer
