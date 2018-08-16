
public class SFTP_Protocol_Test {

	public static void main(String argv[]) {
		int port = 118;
		
		// Start server in its own thread
		Thread serverThread = new Thread(){
			public void run(){
				System.out.println("SFTP Server Starting");
				try {			
					SFTPServer myServer = new SFTPServer(port);
				} catch (Exception e) {
					e.printStackTrace();
				}
		    }
		};
		serverThread.start();
				
		// Start client
		System.out.println("SFTP Client Starting");
		try {
			SFTPClient myClient = new SFTPClient(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\nSFTP Protocol Ended");
	}
}
