import java.io.*;
import java.net.*;

public class SFTPServer {
	
	private JSONHandler loginFileHandler;
	private int loginState = 0; // 0 = not logged in, 1 = logged in, 2 = supplied user id, 3 = supplied account name, 4 = password correct, but no account name
	private int loggedInUserID = 0;
	private String loggedInAccount = null;
	private int fileType = 1; // 0 = Ascii, 1 = Binary, 2 = Continuous
	
	/* 
	 * Constructor
	 */
	public SFTPServer(int port) throws Exception {
		String clientInput;
		String parameters;
		String command;
		String response;
		boolean open = true;
		
		loginFileHandler = new JSONHandler();
		
		// Open socket for receiving
		ServerSocket welcomeSocket = new ServerSocket(port);
		welcomeSocket.setReuseAddress(true);
		Socket connectionSocket = welcomeSocket.accept();
		
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		
		while(open) {
			// Get first 4 characters - this is the command
			clientInput = inFromClient.readLine();
			command = clientInput.substring(0, Math.min(clientInput.length(), 4));
			
			// Handle the specific command
			if (command.equals("DONE")) {
				open = false;
				response = "+";
			} else {
				// Get the parameters, truncate off the first 5 characters
				try {
					parameters = clientInput.substring(5, clientInput.length());
				} catch (Exception e) {
					outToClient.writeBytes("-unknown command \n");
					continue;
				}
				
				if (command.equals("USER")) {
					response = USERCommand(parameters);
				} else if (command.equals("ACCT")) {
					response = ACCTCommand(parameters);
				} else if (command.equals("PASS")) {
					response = PASSCommand(parameters);
				} else if (command.equals("TYPE")) {
						response = TYPECommand(parameters);
				} else {
					response = "-unknown command";
				}
			} 

			// Write response
			outToClient.writeBytes(response + "\n");
		}
		
		welcomeSocket.close();
	}
	
	/* 
	 * Handles the USER command.
	 */
	public String USERCommand(String userID_string) {
		int userID = Integer.parseInt(userID_string);
		int status;
	
		// Check login data for that user id
		status = loginFileHandler.checkUserID(userID);
		
		// Output result of command
		if (status == 0) {
			return "-Invalid user-id, try again";
		} else if (status == 1) {
			loginState = 2;
			loggedInUserID = userID;
			loggedInAccount = null;
			return "+User-id valid, send account and password";
		} else {
			loginState = 1;
			loggedInUserID = userID;
			loggedInAccount = null;
			return "!" + userID + " logged in";
		}
	}
	
	/* 
	 * Handles the ACCT command.
	 */
	public String ACCTCommand(String account) {
		int status;
		
		// If already logged in, no need for account
		if (loginState == 1) { 
			return "!Account valid, logged-in";
		}
		
		// If not logged in, and no user id has been specified
		if (loginState == 0) {
			return "-Invalid account, try again";
		}
		
		// Check login data for that account
		status = loginFileHandler.checkAccount(loggedInUserID, account);
		
		// Output result of command
		if (status == 0) {
			loginState = 2;
			loggedInAccount = null;
			return "-Invalid account, try again";
		} else if (status == 1) {
			loggedInAccount = account;
			if (loginState == 4) {
				loginState = 1;
				return "!Account valid, logged-in";
			} else {
				loginState = 3;	
				return "+Account valid, send password";
			}			
		} else {
			loginState = 1;
			loggedInAccount = account;
			return "!Account valid, logged-in";
		}
	}
	
	/* 
	 * Handles the PASS command.
	 */
	public String PASSCommand(String password) {
		int status;
		
		// If already logged in, no need for password
		if (loginState == 1) { 
			return "!Logged in";
		}
		
		// If not logged in, and no user id has been specified
		if (loginState == 0) {
			return "-Wrong password, try again";
		}
		
		// Check login data for that account
		status = loginFileHandler.checkPassword(loggedInUserID, loggedInAccount, password);
		
		// Output result of command
		if (status == 0) {
			return "-Wrong password, try again";
		} else if (status == 1) {
			loginState = 4;
			return "+Send account";
		} else {
			loginState = 1;
			return "!Logged in";
		}
	}
	
	/*
	 * Handles the TYPE command 
	 */
	public String TYPECommand(String type) {
		if (loginState == 1) {
			if (type.equals("A")) {
				fileType = 0;
				return "+Using Ascii mode";
			} else if (type.equals("B")) {
				fileType = 1;
				return "+Using Binary mode";
			} else if (type.equals("C")) {
				fileType = 2;
				return "+Using Continuous mode";
			} else {
				return "-Type not valid";
			}
		} else {
			return "-Please Login";
		}
	}
}
