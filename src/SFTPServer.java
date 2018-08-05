import java.io.*;
import java.net.*;
import java.util.Date;

public class SFTPServer {
	
	private boolean activeServer = true;
	private JSONHandler loginFileHandler;
	private int loginState = 0; // 0 = not logged in, 1 = logged in, 2 = supplied user id, 3 = supplied account name, 4 = password correct, but no account name
	private int loggedInUserID = 0;
	private String loggedInAccount = null;
	private int fileType = 1; // 0 = Ascii, 1 = Binary, 2 = Continuous
	private String currentDir = "C:/";
	private String requestedDir = null;
	private String renameFile = null;
	
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
		PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
		
		if (activeServer) {
			outToClient.println("+UoA-725 SFTP Service\0");
		} else {
			outToClient.println("-UoA-725 Out to Lunch\0");
		}
		
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
					parameters = clientInput.substring(5, clientInput.length()-1);
				} catch (Exception e) {
					outToClient.println("-unknown command\0");
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
				} else if (command.equals("LIST")) {
					response = LISTCommand(parameters);
				} else if (command.equals("CDIR")) {
					response = CDIRCommand(parameters);
				} else if (command.equals("KILL")) {
					response = KILLCommand(parameters);
				} else if (command.equals("NAME")) {
					response = NAMECommand(parameters);
				} else if (command.equals("TOBE")) {
					response = TOBECommand(parameters);
				} else {
					response = "-unknown command";
				}
			} 

			// Write response
			outToClient.println(response + "\0");
		}
		
		welcomeSocket.close();
	}
	
	/* 
	 * Handles the USER command.
	 */
	public String USERCommand(String userID_string) {
		int userID; 
		int status;
		
		try {
			userID = Integer.parseInt(userID_string);
		} catch (Exception e) {
			return "-Invalid user-id, try again";
		}
	
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
			if (requestedDir != null) {
				String output = CDIRCommand(requestedDir);
				requestedDir = null;
				return output;
			}
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
			if (requestedDir != null) {
				String output = CDIRCommand(requestedDir);
				requestedDir = null;
				return output;
			}
			return "!Logged in";
		}
	}
	
	/*
	 * Handles the TYPE command 
	 */
	public String TYPECommand(String type) {
		if (loginState == 1) { // Check Login
			if (type.equals("A")) {
				fileType = 0; // Switch file type
				return "+Using Ascii mode";
			} else if (type.equals("B")) {
				fileType = 1; // Switch file type
				return "+Using Binary mode";
			} else if (type.equals("C")) {
				fileType = 2; // Switch file type
				return "+Using Continuous mode";
			} else {
				return "-Type not valid";
			}
		} else {
			return "-Please Login";
		}
	}
	
	/*
	 * Handles the LIST command 
	 */
	public String LISTCommand(String parameters) {
		String format;
		String path;
		
		if (loginState == 1) { // Check the login
			try {
				format = parameters.substring(0, 1); // Split into verbose flag and path
				if (parameters.length() < 3) {
					path = currentDir;
				} else {
					path = parameters.substring(2, parameters.length());
				}
			} catch (Exception e) {
				return "-Format or directory not valid";
			}
			
			if (format.equals("F")) { // Call listDir()
				return listDir(path, false);
			} else if (format.equals("V")) {
				return listDir(path, true);
			} else {
				return "-Format not valid";
			}
		} else {
			return "-Please Login";
		}
	}
	
	/*
	 * Helper function for LISTCommand()
	 */
	private String listDir(String pathString, boolean verbose) {
		String output = "";
		
		File path = new File(pathString); // Create a path
		try {
			File[] files = path.listFiles(); // List all files
			output = output + "+" + pathString + "\r\n"; // Append directory name
			for (File file : files) {
				output = output + file.getName(); // Append file/folder names
				if (verbose) {
					if (file.isFile()) { // Append if it is a file or folder
						output = output + "\t\t File"; 
					} else {
						output = output + "\t\t Folder";
					}
					output = output + "\t\t Size: " + file.length() +  " B \t\t Last Modified: " + new Date(file.lastModified()); // Append size and date
				}
				output = output + "\r\n";
			}
		} catch (Exception e) {
			if (e.getMessage() == null) { // Path doesn't exist
				return "-Directory path doesn't exist";
			}
			return "-" + e.getMessage(); // Some other error
		}
		
		return output;
	}
	
	/*
	 * Handles the CDIR command 
	 */
	public String CDIRCommand(String newDir) {
		if (loginState == 0) { // Check if a userID is specified
			return "-Can’t connect to directory because: No UserID";
		} else { 
			String result = listDir(newDir, false);
			if (result.equals("-Directory path doesn't exist")) { // Check if the directory exists, reuses listDir()
				return "-Can’t connect to directory because: Directory doesn't exist";
			}

			if (loginState == 1) { // User is logged in, can change directory		
				currentDir = newDir;
				return "+!Changed working dir to " + newDir;
			} else { // User is not logged in, flag the directory that the user wants
				requestedDir = newDir;
				return "+directory ok, send account/password";
			}
		}
	}
	
	/*
	 * Handles the KILL command 
	 */
	public String KILLCommand(String fileSpec) {
		if (loginState == 1) { 
			File path = new File(currentDir + "/" + fileSpec); // Local files only. Absolute paths don't work
			if (path.exists()) { // Check if file exists
				if (path.delete()) { // Attempt to delete file
					return "+" + fileSpec + " deleted";
				} else {
					return "-Not deleted because of an unknown error";
				}
			} else {
				return "-Not deleted because file doesn't exist";
			}
		} else { 
			return "-Not deleted because client is not logged in";
		}
	}
	
	/*
	 * Handles the NAME command 
	 */
	public String NAMECommand(String fileSpec) {
		if (loginState == 1) { 
			File path = new File(currentDir + "/" + fileSpec); // Local files only. Absolute paths don't work
			if (path.exists()) { // Check if file exists
				renameFile = fileSpec;
				return "+File exists";
			} else {
				renameFile = null;
				return "-Can’t find " + fileSpec;
			}
		} else { 
			return "-Please log in";
		}
	}
	
	/*
	 * Handles the TOBE command 
	 */
	public String TOBECommand(String fileSpec) {
		if (loginState == 1) {
			if (renameFile == null) {
				return "-File wasn’t renamed because NAME not specified";
			}
			File path = new File(currentDir + "/" + renameFile); // Local files only. Absolute paths don't work
			File newPath = new File(currentDir + "/" + fileSpec);
			if (path.renameTo(newPath)) {
				return "+" + renameFile + " renamed to " + fileSpec;
			}
			return "-File wasn't renamed due to an unknown error";
		} else { 
			return "-Please log in";
		}
	}
}
