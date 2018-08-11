import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class SFTPClient {
	private int fileType = 1; // 0 = Ascii, 1 = Binary, 2 = Continuous
	private int fileLength = 0;
	private String fileName = null;
	private String fileToSend = null;
	private boolean noFile = false;
	private OutputStream outputStream;
	
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
		
		// Start socket
		Socket clientSocket = new Socket("localhost", port);
		clientSocket.setReuseAddress(true);
		
		//Setups
		PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
		outputStream = clientSocket.getOutputStream();
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		
		// Read user input, prepare response line
		fromServer = inFromServer.readLine();
		System.out.println("FROM SERVER: " + fromServer);

		while(open) {
			System.out.println("\nTO SERVER:");
			
			toServer = inFromUser.readLine();
			
			// Check what the command is
			command = toServer.substring(0, Math.min(toServer.length(), 4));
			
			// If a SIZE command but the file doesn't exist
			if (command.equals("SIZE") && noFile) {
				System.out.println("File doesn't exist on host system. Aborting command.");
				continue;
			}
			
			// Send command to server
			outToServer.println(toServer + "\0");
			
			// Receive a file
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
			} else if (command.equals("STOR")) {
				// Prepare to send a file
				try {
					fileToSend = toServer.substring(9, toServer.length());
					File file = new File(fileToSend);
					if (!file.exists()) {
						noFile = true;
					} else {
						noFile = false;
					}
				} catch (Exception e) {

				}
			}
			
			// Get the reply from the server
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
			
			// Print the reply
			System.out.println("FROM SERVER: " + fromServer);
			
			// Respond to positive replies
			if (fromServer.substring(0, 1).equals("+")) {
				if (command.equals("DONE")) {
					// Close connection
					open = false;
				} else if (command.equals("STOP")) {
					continue;
				} else if (command.equals("SIZE")) {
					// Send file
					File path = new File(fileToSend);
					try {
						byte[] fileContent = Files.readAllBytes(path.toPath());
						outputStream.write(fileContent);
					} catch (IOException e) {
						e.printStackTrace();
					}
					fileToSend = null;
					
					// Check file reply
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
					
					// Print file reply
					System.out.println("FROM SERVER: " + fromServer);
				} else {
					// Set parameters for file sending
					parameters = toServer.substring(5, toServer.length());
					if (command.equals("TYPE")) {			
						if (parameters.equals("A")) { fileType = 0; } 
						else if (parameters.equals("B")) { fileType = 1; } 
						else if (parameters.equals("C")) {fileType = 2; }
					}
				}
			}
			
			// Check file length and name
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
