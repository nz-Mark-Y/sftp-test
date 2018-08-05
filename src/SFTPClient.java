import java.io.*;
import java.net.*;

public class SFTPClient {
	private int fileType = 1; // 0 = Ascii, 1 = Binary, 2 = Continuous
	private int fileLength = 0;
	private String fileName = null;
	
	/* 
	 * Constructor
	 */
	public SFTPClient(int port) throws Exception {
		String toServer;
		String fromServer;
		String command;
		String parameters;
		boolean open = true;
		
		int letter;
		StringBuilder sb = new StringBuilder();
		
		Socket clientSocket = new Socket("localhost", port);
		clientSocket.setReuseAddress(true);
		
		PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		fromServer = inFromServer.readLine();
		System.out.println("FROM SERVER: " + fromServer);

		while(open) {
			System.out.println("\nTO SERVER:");
			
			toServer = inFromUser.readLine();
			outToServer.println(toServer + "\0");
			
			command = toServer.substring(0, Math.min(toServer.length(), 4));
			
			if (command.equals("SEND") && fileName != null) {
				byte[] receivedFile = new byte[fileLength];
				for (int i=0; i<fileLength; i++) {
					receivedFile[i] = (byte) clientSocket.getInputStream().read();
				}
				FileOutputStream stream = new FileOutputStream(fileName);
				try {
				    stream.write(receivedFile);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				    stream.close();
				}
				System.out.println(fileName + " received");
				inFromServer.readLine();
				fileName = null;
				continue;
			} 
			
			while (true) {
				letter = inFromServer.read();
				sb.append((char) letter);
				if (letter == 0) {
					inFromServer.readLine();
					break;
				}
			}
			fromServer = sb.toString();
			sb.setLength(0);
			
			System.out.println("FROM SERVER: " + fromServer);

			if (fromServer.substring(0, 1).equals("+")) {
				if (command.equals("DONE")) {
					open = false;
				} else if (command.equals("STOP")) {
					continue;
				} else {
					parameters = toServer.substring(5, toServer.length());
					if (command.equals("TYPE")) {			
						if (parameters.equals("A")) { fileType = 0; } 
						else if (parameters.equals("B")) { fileType = 1; } 
						else if (parameters.equals("C")) {fileType = 2; }
					}
				}
			}
			
			if (command.equals("RETR")) {
				try {
					fileName = toServer.substring(5, toServer.length());
					fileLength = Integer.parseInt(fromServer.substring(0,fromServer.length()-1));
				} catch (Exception e) {
					fileName = null;
					continue;
				}
			}
		}
		
		clientSocket.close();
	}
}
