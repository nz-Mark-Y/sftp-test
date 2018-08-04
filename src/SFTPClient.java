import java.io.*;
import java.net.*;

public class SFTPClient {
	
	/* 
	 * Constructor
	 */
	public SFTPClient(int port) throws Exception {
		String toServer;
		String fromServer;
		boolean open = true;
		
		Socket clientSocket = new Socket("localhost", port);
		clientSocket.setReuseAddress(true);
		clientSocket.setKeepAlive(true);
		
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		while(open) {
			System.out.println("\nTO SERVER:");
			
			toServer = inFromUser.readLine();
			
			outToServer.writeBytes(toServer + '\n');
			fromServer = inFromServer.readLine();
			
			System.out.println("FROM SERVER: " + fromServer);	
			
			if (toServer.equals("DONE")) {
				open = false;
			}
		}
		
		clientSocket.close();
	}
}
