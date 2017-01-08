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
import java.util.concurrent.LinkedBlockingQueue;

public class WebServer {
	private ServerSocket ss; //A server socket listens on a port number for incoming requests
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	private volatile boolean keepRunning = true;
	private String directory;
	private int port;
	private final String POISON_PILL = "POISON";
	
	//An array blocking queue
	BlockingQueue<String> bq = new ArrayBlockingQueue<String>(100);

	private WebServer(String port, String dir){
		this.directory = dir;
		//convert string argument for port to integer
		this.port = Integer.parseInt(port);

		try { 											
			ss = new ServerSocket(this.port); //Start the server socket listening on port enterted on startup
			Thread server = new Thread(new Listener(), "Web Server Listener"); //We can also name threads
			server.setPriority(Thread.MAX_PRIORITY); //Ask the Thread Scheduler to run this thread as a priority
			server.start(); //The Hollywood Principle - Don't call us, we'll call you
			
			//Create new Thread to handle the Blocking Queue
			Thread logger = new Thread(new Logger(), "Logger");
			logger.setPriority(Thread.MAX_PRIORITY); 
			logger.start();//start the Thread
			
			System.out.println("Server started and listening on port " + this.port);
			
		} catch (IOException e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
			System.out.println("Yikes! Something bad happened..." + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		//The application requires 2 arguments to start. Port number and the path to your files
		if(args.length < 2){
			System.out.println("You must enter a port number and path to your files.");
		}
		else{
			new WebServer(args[0], args[1]); //Create an instance of a WebServer. This fires the constructor of WebServer() above on the main stack 
		}	
	}//Main
	
	private class Listener implements Runnable{ //A Listener IS-A Runnable
		
		public void run() {
			int counter = 0; 
			while (keepRunning){ 
				try {
					
					Socket s = ss.accept(); 
					System.out.println("Socket Accepted");
					new Thread(new HTTPRequest(s), "T-" + counter).start(); //Give the new job to the new worker and tell it to start work
					System.out.println("Thread Started!!");
					counter++; //Increment counter
				} catch (IOException e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
					System.out.println("Error handling incoming request..." + e.getMessage());
				}
			}
		}//RUN
	}//End of inner class Listener

	private class HTTPRequest implements Runnable{
		private Socket sock; 
		
		private HTTPRequest(Socket request) { 
			this.sock = request; 
		}

        public void run() {
            
        	try{ 
            	while(keepRunning){
                   	in = new ObjectInputStream(sock.getInputStream());
                   	out = new ObjectOutputStream(sock.getOutputStream());
	                Object command = in.readObject(); 
	               
	            	if(command.toString().equals("2")){
	            		bq.put("INFO Listing requested by " + sock.getInetAddress() + " at " + LocalDateTime.now() + "\n");
	            		GetListFiles getFiles = new GetListFiles(directory);
	            		String files = getFiles.files().toString();
	            		message = files;
	            		out.writeObject(message);
	            		out.flush();    		
	            		
	            	}
	            	else if(command.toString().equals("3")){
	            		bq.put("INFO File requested by " + sock.getInetAddress() + " at " + LocalDateTime.now() + "\n");
	                    message = "What file do you wish to download?";
	                	out.writeObject(message);
	                    out.flush();
	                    
	                    command = in.readObject();
	                    System.out.println(command.toString());
	         
	                    File targetFile = new File(directory + command.toString());
	                    System.out.println(targetFile.exists());
	                    
	                    if(targetFile.exists()){   
	                    	message = "y";   	
	                    }
	                    else{
	                    	message = "n";
	                    	bq.put("WARN File requested by " + sock.getInetAddress() + " at " + LocalDateTime.now() + " was not found!\n");
	                    }
	                	out.writeObject(message);
	                    out.flush();
	                    
	                    if(targetFile.exists()){
		                    byte[] mybytearray = new byte[(int) targetFile.length()];
		                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(targetFile));
		                    bis.read(mybytearray, 0, mybytearray.length);
		                    
		                    out.write(mybytearray, 0, mybytearray.length);
		                    out.flush();	
	                    }
	            	}               	
            	}//while
            	         
            } catch (FileNotFoundException e) {
            	System.out.println("File not Found on Server");
            } catch (EOFException e) {
            	System.out.println("Exiting HTTP Request");
            	try {
            		//This how I kill the blocking queue
            		//Upon option 4 being selected the string "POISON_PILL" is added to the blocking queue
					bq.put(POISON_PILL);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
				    		//break the 
				    		break;  		
				    	}
				    	else{
					    	//Here true is to append the content to file
					    	FileWriter fw = new FileWriter(file,true);
					    	//BufferedWriter writer give better performance
					    	BufferedWriter bw = new BufferedWriter(fw);
					    	bw.write(bq.take());
							bw.close();
				    	}

					} catch (InterruptedException e) {
						System.out.println("Thread Interutpted: " + e.getMessage());
					} 		
		    	}
			} catch (IOException e){
				System.out.println("I/O Exception: " + e.getMessage());
			}			
		}//Run
	}//End of inner class Logger
}//WebServer
