package ie.gmit.sw.client;

public class Context {
	
	protected static final String file = "conf.xml";
	private String username;
	private String serverHost;
	private int serverPort;
	private String downloadDir;
	
	public Context(){
		
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getServerHost() {
		return serverHost;
	}
	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	public String getDownloadDir() {
		return downloadDir;
	}
	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}
	
	@Override
	public String toString() {
		return "Context [username=" + username + ", serverHost=" + serverHost + ", serverPort=" + serverPort
				+ ", downloadDir=" + downloadDir + "]";
	}
	
	
}
