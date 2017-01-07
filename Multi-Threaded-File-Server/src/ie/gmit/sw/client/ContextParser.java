package ie.gmit.sw.client;

import javax.xml.parsers.*;
import org.w3c.dom.*;

public class ContextParser {
	private Context ctx;

	public ContextParser(Context ctx) {
		super();
		this.ctx = ctx;
		try {
			init();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init() throws Throwable{
		/* These three lines are part of JAXP (Java API for XML Processing) and are designed to
		 * completely encapsulate how a DOM node tree in manufactured. The concrete classes that
		 * are doing the actual work are part of the Apache Xerces project. JAXP shields us from
		 * having to know and understand the complexity of Xerces through encapsulating the
		 * process.
		 */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(Context.file);
		
		/* The type Document in the line above is an org.w3c.dom.Document. From this
		 * point on in the method, we will only use the DOM "STANDARD" to do the
		 * processing. DOM is language neutral and completely abstract. As a result,
		 * it is completely stable! The DOM standard hasn't changed much since the
		 * year 2000...! Abstractions are highly stable and can be relied upon. 
		 */
		Element root = doc.getDocumentElement(); //Get the root of the node tree
		NodeList children = root.getChildNodes(); //Get the child node of the root
		
		if(root.hasAttribute("username")){
			ctx.setUsername(root.getAttributes().item(0).getNodeValue());
			
		}
		
		//Find the elements database and driver
		for (int i = 0; i < children.getLength(); i++){ //Loop over the child nodes
			Node next = children.item(i); //Get the next child
			
			if (next instanceof Element){ //Check if it is an element node. There are 12 different types of Node
				Element e = (Element) next; //Cast the general node to an element node
				
				if(e.getNodeName().equals("server-host")){
					String host = e.getFirstChild().getNodeValue();
					ctx.setServerHost(host);
				}
				else if(e.getNodeName().equals("server-port")){
					String port = e.getFirstChild().getNodeValue();
					ctx.setServerPort(Integer.parseInt(port));
				}
				else if(e.getNodeName().equals("download-dir")){
					String downloadDir = e.getFirstChild().getNodeValue();
					ctx.setDownloadDir(downloadDir);
					System.out.println(downloadDir);
				}						
			}
		}	
		
	}
	
	public Context getCtx() {
		return ctx;
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
	}
}
