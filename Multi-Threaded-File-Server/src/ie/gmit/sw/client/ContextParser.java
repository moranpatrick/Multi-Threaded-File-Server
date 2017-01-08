package ie.gmit.sw.client;

import javax.xml.parsers.*;
import org.w3c.dom.*;

public class ContextParser {
	private Context ctx;

	public ContextParser(Context ctx) {
		super();
		this.ctx = ctx;
		try {
			//init is called in the contructer
			init();
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void init() throws Throwable{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(Context.file);

		Element root = doc.getDocumentElement(); //Get the root of the node tree
		NodeList children = root.getChildNodes(); //Get the child node of the root
		//if root name is "username"
		if(root.hasAttribute("username")){
			ctx.setUsername(root.getAttributes().item(0).getNodeValue());		
		}
		//Loop over the child nodes
		for (int i = 0; i < children.getLength(); i++){ 
			//Get the next child
			Node next = children.item(i); 
			
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
				}						
			}
		}//for			
	}//init()
	
	public Context getCtx() {
		return ctx;
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
	}
}//ContextParser
