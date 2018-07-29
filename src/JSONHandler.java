import java.io.*;
import org.json.*;

public class JSONHandler {
	private JSONArray usersArray;
	
	public JSONHandler() throws Exception {
		BufferedReader fileReader = new BufferedReader(new FileReader("login_data.txt"));
		
		String text = "";
		String line = null;
		while((line = fileReader.readLine()) != null) {
            text = text + line;
        }   
        fileReader.close();  
		 
		JSONObject obj = new JSONObject(text);
		usersArray = obj.getJSONArray("users");
	}
	
	public int checkUserID(int userID) {
		for (int i=0; i<usersArray.length(); i++) {
			try {
				if (usersArray.getJSONObject(i).getInt("user-id") == userID) {
					if ((usersArray.getJSONObject(i).getJSONArray("accounts").length() == 0) && (!usersArray.getJSONObject(i).has("password"))) {
						return 2; // Logged In
					} else {
						return 1; // Not Logged In
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return 0; // Not Found
	}
	
	public int checkAccount(int userID, String accountName) {
		for (int i=0; i<usersArray.length(); i++) {
			try {
				if (usersArray.getJSONObject(i).getInt("user-id") == userID) {
					if (usersArray.getJSONObject(i).getJSONArray("accounts").length() == 0) {
						return 1; // Account not required, needs password
					}
			
					for (int j=0; j<usersArray.getJSONObject(i).getJSONArray("accounts").length(); j++) {
						if (usersArray.getJSONObject(i).getJSONArray("accounts").getJSONObject(j).getString("account").equals(accountName)) {
							if (usersArray.getJSONObject(i).has("password")) {
								return 1; // Account good, needs password
							} else {
								return 2; // Logged In, password not required
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return 0; // Not Found
	}
	
	public int checkPassword(int userID, String accountName, String password) {
		for (int i=0; i<usersArray.length(); i++) {
			try {
				if (usersArray.getJSONObject(i).getInt("user-id") == userID) {
					if (usersArray.getJSONObject(i).has("password")) {
						if (usersArray.getJSONObject(i).getString("password").equals(password)) {
							if ((accountName == null) && (usersArray.getJSONObject(i).getJSONArray("accounts").length() != 0)) {
								return 1; // Needs account name
							} else {
								return 2; // Logged in
							}
						} else {
							return 0; // Wrong password
						}
					} else {
						return 1; // No password, therefore account name required
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}	
		
		return 0; // Not Found
	}
}
