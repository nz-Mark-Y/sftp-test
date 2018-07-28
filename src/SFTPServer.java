import java.io.*;
import java.net.*;

public class SFTPServer {

	public SFTPServer(int port) throws Exception {
		String clientInput;
		String command;
		String response;
		boolean open = true;
		
		ServerSocket welcomeSocket = new ServerSocket(port);
		welcomeSocket.setReuseAddress(true);
		
		while(open) {
			Socket connectionSocket = welcomeSocket.accept();
			
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			clientInput = inFromClient.readLine();
			command = clientInput.substring(0, Math.min(clientInput.length(), 4));
			if (command.equals("DONE")) {
				open = false;
				response = "+";
			} else if (command.equals("USER")) {
				response = "hi";
			} else {
				response = "-unknown command";
			}

			outToClient.writeBytes(response + "\n");
		}
		
		welcomeSocket.close();
	}
}
