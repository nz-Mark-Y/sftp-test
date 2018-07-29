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
		
		while(open) {
			System.out.println("\nTO SERVER:");
			
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			Socket clientSocket = new Socket("localhost", port);
			clientSocket.setReuseAddress(true);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			toServer = inFromUser.readLine();
			outToServer.writeBytes(toServer + '\n');
			fromServer = inFromServer.readLine();
			
			System.out.println("FROM SERVER: " + fromServer);
			clientSocket.close();
			
			if (toServer.equals("DONE")) {
				open = false;
			}
		}
	}
}
