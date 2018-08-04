import java.io.*;
import java.net.*;

public class SFTPClient {
	private int fileType = 1; // 0 = Ascii, 1 = Binary, 2 = Continuous
	
	/* 
	 * Constructor
	 */
	public SFTPClient(int port) throws Exception {
		String toServer;
		String fromServer;
		String command;
		String parameters;
		boolean open = true;
		
		Socket clientSocket = new Socket("localhost", port);
		clientSocket.setReuseAddress(true);
		clientSocket.setKeepAlive(true);
		
		PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		fromServer = inFromServer.readLine();
		System.out.println("FROM SERVER: " + fromServer);	

		while(open) {
			System.out.println("\nTO SERVER:");
			
			toServer = inFromUser.readLine();
			
			outToServer.println(toServer + "\0");
			fromServer = inFromServer.readLine();
			
			System.out.println("FROM SERVER: " + fromServer);	
			command = toServer.substring(0, Math.min(toServer.length(), 4));
			
			if (fromServer.substring(0, 1).equals("+")) {
				if (command.equals("DONE")) {
					open = false;
				} else {
					parameters = toServer.substring(5, toServer.length());
					if (command.equals("TYPE")) {			
						if (parameters.equals("A")) { fileType = 0; } 
						else if (parameters.equals("B")) { fileType = 1; } 
						else if (parameters.equals("C")) {fileType = 2; }
					}
				}
			}
		}
		
		clientSocket.close();
	}
}
