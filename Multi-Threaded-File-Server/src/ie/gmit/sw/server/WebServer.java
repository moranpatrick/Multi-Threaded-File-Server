package ie.gmit.sw.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.ws.handler.MessageContext;


public class WebServer {
	private ServerSocket ss; //A server socket listens on a port number for incoming requests
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	
	//The first 1024 ports require administrator privileges. We'll use 8080 instead. The range 
	//of port numbers runs up to 2 ^ 16 = 65536 ports.
	private static final int SERVER_PORT = 7777;  
	
	//The boolean value keepRunning is used to control the while loop in the inner class called
	//Listener. The volatile keyword tells the JVM not to cache the value of keepRunning during
	//optimisation, but to check it's value in memory before using it.
	private volatile boolean keepRunning = true;
	private String directory;
	
	
	//A null constructor for the WebServer class
	private WebServer(String dir){
		this.directory = dir;
		try { //Try the following. If anything goes wrong, the error will be passed to the catch block
			
			ss = new ServerSocket(SERVER_PORT); //Start the server socket listening on port 8080
			
			/* A Thread is a worker. A runnable is a job. We'll give the worker thread called "server"
			 * the job of handling incoming requests from clients.
			 * Note: calling start results in a new JVM stack being created. The run() method of the Thread
			 * or Runnable will be placed on the new stack and executed when the Thread Scheduler (consider
			 * this a cantankerous and uncommunicative part of the JVM) decides so. There is absolutely NO
			 * GUARANTEE of either order or execution time. We can however ask the Thread Scheduler 
			 * (politely) to run a thread as a max, min or normal priority. 
			 */
			Thread server = new Thread(new Listener(), "Web Server Listener"); //We can also name threads
			server.setPriority(Thread.MAX_PRIORITY); //Ask the Thread Scheduler to run this thread as a priority
			server.start(); //The Hollywood Principle - Don't call us, we'll call you
			
			System.out.println("Server started and listening on port " + SERVER_PORT);
			
		} catch (IOException e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
			System.out.println("Yikes! Something bad happened..." + e.getMessage());
		}
	}
	
	//A main method is required to start a standard Java application
	public static void main(String[] args) {
		if(args.length < 2){
			System.out.println("You must enter a port number and path.");
		}
		else{
			new WebServer(args[1]); //Create an instance of a WebServer. This fires the constructor of WebServer() above on the main stack 
		}
		
	}
	
	/* The inner class Listener is a Runnable, i.e. a job that can be given to a Thread. The job that
	 * the class has been given is to intercept incoming client requests and farm them out to other
	 * threads. Each client request is in the form of a socket and will be handled by a separate new thread.
	 */
	private class Listener implements Runnable{ //A Listener IS-A Runnable
		
		//The interface Runnable declare the method "public void run();" that must be implemented
		public void run() {
			int counter = 0; //A counter to track the number of requests
			while (keepRunning){ //Loop will keepRunning is true. Note that keepRunning is "volatile"
				try { //Try the following. If anything goes wrong, the error will be passed to the catch block
					
					Socket s = ss.accept(); //This is a blocking method, causing this thread to stop and wait here for an incoming request
					System.out.println("New Request");
					/* If we get to this line, it means that a client request was received and that the socket "s" is a real network
					 * connection between some computer and this programme. We'll farm out this request to a new Thread (worker), 
					 * allowing us to handle the next incoming request (we could have many requests hitting the server at the same time),
					 * so we have to be able to handle them quickly.
					 */
					new Thread(new HTTPRequest(s), "T-" + counter).start(); //Give the new job to the new worker and tell it to start work
					counter++; //Increment counter
				} catch (IOException e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
					System.out.println("Error handling incoming request..." + e.getMessage());
				}
			}
		}
	}//End of inner class Listener
	
	/* The inner class HTTPRequest is a Runnable, i.e. a job that can be given to a Thread. The job that
	 * the class has been given is to handle an individual client request, by reading information from the
	 * socket's input stream (bytes) and responding by sending information to the socket's output stream (more
	 * bytes).
	 */
	private class HTTPRequest implements Runnable{
		private Socket sock; //A specific socket connection between some computer on a network and this programme
		
		private HTTPRequest(Socket request) { //Taking the client socket as a constructor enables the Listener class above to farm out the request quickly
			this.sock = request; //Assign to the instance variable sock the value passed to the constructor. 
		}

		//The interface Runnable declare the method "public void run();" that must be implemented
        public void run() {
            try{ //Try the following. If anything goes wrong, the error will be passed to the catch block
            	
               	in = new ObjectInputStream(sock.getInputStream());
               	out = new ObjectOutputStream(sock.getOutputStream());
                Object command = in.readObject(); //Deserialise the request into an Object
               
            	if(command.toString().equals("2")){
            		System.out.println("Asked to print files");
            		GetListFiles getFiles = new GetListFiles(directory);
            		String files = getFiles.files().toString();
            		message = files;
            		out.writeObject(message);
            		out.flush();    		
            		
            	}
            	else if(command.toString().equals("3")){
                    message = "What file do you wish to download?";
                	out.writeObject(message);
                    out.flush();
                    
                    command = in.readObject();//Deserialise the request into an Object
                    System.out.println(command.toString());
         
                                        
                    
                    File targetFile = new File(directory + command.toString());
                    System.out.println(targetFile.exists());
                    
                    byte[] mybytearray = new byte[(int) targetFile.length()];
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(targetFile));
                    bis.read(mybytearray, 0, mybytearray.length);
                    
                    out.write(mybytearray, 0, mybytearray.length);
                    out.flush();
                    
            		
            	}
            	
                
            } catch (Exception e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
            	System.out.println("Error processing request from " + sock.getRemoteSocketAddress());
            	e.printStackTrace();
            }
        }
	}//End of inner class HTTPRequest
}//End of class WebServer

