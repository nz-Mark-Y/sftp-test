# sftp-test
Client and Server Implementation for SFTP (Simple File Transfer Protocol)

# IMPORTANT
* Client side saves and sends files local to the main project folder
* Server side saves and sends files to the directory the client specifies
* The default server directory is C:/
* All file (LIST, KILL, STOR, RETR) actions work for local paths in the current directory only
* CDIR works for absolute paths only
* When sending files to the server, ensure that the folder is not protected by administrative priviledges

# Testing
* Open the project in Eclipse
* Run the project to create both client and server instances
* Type commands in the terminal as specified in the documentation, excluding NULLS
* For example, "LIST F C:/Users/Foo" to list all the files and folders in the C:/Users/Foo directory, non-verbose
* Press ENTER to send the command to the server

* Before attempting to rerun the program, ensure to use the DONE command to safely disconnect the server and client

* Logins are stored in the login_data.txt file, in JSON format
* Each login is composed of a user ID, optional user password, and optional account name

* When using the STOR command, you will have to enter the size of the file yourself with SIZE afterwards
* This can easily be checked by right-clicking on the file and checking its size (in bytes)

* For each command, ensure there is a single space between each 4 letter command and the parameters
* Additionally, there should be a single space between each parameter

* For each command, the output should respond in the same way as specified in the SFTP protocol provided
* Additionally, if the command and parameters are not in the correct format, then an "-Invalid command" will be returned
* Finally, if the user is not logged in and is attempting a command which requires login, then a "-Please login" will be returned



