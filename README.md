# sftp-test
Client and Server Implementation for SFTP (Simple File Transfer Protocol)

# IMPORTANT
* Client side saves and sends files local to the main project folder
* Server side saves and sends files to the directory the cient specifies
* The default server directory is C:/
* All file (LIST, KILL, STOR, RETR) actions work for local paths in the current directory only
* CDIR works for absolute paths only
* When sending files to the server, ensure that the folder is not protected by administrative priviledges

# Testing
* Open the project in Eclipse
* Run the project to create both client and server instances
* Type commands in the terminal as specified in the documentation, excluding NULLS
* For example, "LIST F C:/Users/Foo" to list all the files and folders in the C:/Users/Foo directory, non-verbose

* Logins are stored in the login_data.txt file, in JSON format
* Each login is composed of a user ID, optional user password, and optional account name

* When using the STOR command, you will have to enter the size of the file yourself with SIZE afterwards
* This can easily be checked by right-clicking on the file and checking its size (in bytes)



