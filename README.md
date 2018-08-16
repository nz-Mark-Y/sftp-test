# sftp-test
Client and Server Implementation for SFTP (Simple File Transfer Protocol)

# IMPORTANT
* Client side saves and sends files local to the main project folder
* Server side saves and sends files to the directory the client specifies
* The default server directory is C:/
* All file (KILL, STOR, RETR) actions work for local paths in the current directory only
* CDIR works for absolute paths only
* LIST with no parameters works for the current directory, but when specifying a file path, please specify an absolute path
* When sending files to the server, ensure that the folder is not protected by administrative priviledges
* Additionally, please do not try and send empty files

# Testing
* Open the project in Eclipse
* Run the project to create both client and server instances
* Type commands in the terminal as specified in the documentation, excluding NULLS
* For example, "LIST F C:/Users/Foo" to list all the files and folders in the C:/Users/Foo directory, non-verbose
* Press ENTER to send the command to the server

* Before attempting to rerun the program, ensure to use the DONE command to safely disconnect the server and client

## Logins
* Logins are stored in the login_data.txt file, in JSON format
* Each login is composed of a user ID, optional user password, and optional account name

## STOR File Sizes
* When using the STOR command, you will have to enter the size of the file yourself with SIZE afterwards
* This can easily be checked by right-clicking on the file and checking its size (in bytes)

## Spaces
* For each command, ensure there is a single space between each 4 letter command and the parameters
* Additionally, there should be a single space between each parameter

## Expected Command Output
* For each command, the output should respond in the same way as specified in the SFTP protocol provided
* Additionally, if the command and parameters are not in the correct format, then an "-Invalid command" will be returned
* Finally, if the user is not logged in and is attempting a command which requires login, then a "-Please login" will be returned

# Test Cases
## USER
* "USER 1" should cause the server to reply that account and password should then be entered
* "USER 3" should cause the server to reply that the user is logged in, as this user ID has no corresponding account or password
* "USER 6" should cause the server to reply that the user ID does not exist

## ACCT 
* "USER 1" to specify to the server which user we want to login, then:
* "ACCT admin" to specify to the server which account we want to use - this will cause the server to reply that a password should be entered
* If the password has already been entered (i.e USER 1 => PASS admin => ACCT admin) then the server will reply that the user is logged in
* Incorrect account names will cause the server to reply that the account name does not exist and the account name must be respecified

## PASS
* "USER 1" to specify to the server which user we want to login, then:
* "PASS admin" to specify to the server our user password - this will cause the server to reply that an account name should be entered
* If the account name has already been entered (i.e USER 1 => ACCT admin => PASS admin) then the server will reply that the user is logged in
* Incorrect passwords will cause the server to reply that the password is incorrect and must be reentered

## TYPE
* "USER 3" is the quickest way to login
* "TYPE A" or "TYPE B" or "TYPE C" will cause the specified file transfer type to be selected

## LIST
* "USER 3" is the quickest way to login
* "LIST F" will then display the contents of the current directory - which is C:/ by default
* "LIST V C:/Program Files" will display the contents of program files, with additional data

## CDIR
* "USER 3" to login
* "CDIR C:/Users" will change the current working directory to C:/Users
* Use "LIST F" to prove that this has happened

## KILL
* Create a file called "toDelete.txt" in the C:/Users/Public/Public Documents directory 
* "USER 3" to login
* "CDIR C:/Users/Public/Documents" to change the working directory
* "KILL toDelete.txt" to delete the file
* Use "LIST F" to prove that this has happened

## NAME 
* Create a file called "toRename.txt" in the C:/Users/Public/Public Documents directory 
* "USER 3" to login
* "CDIR C:/Users/Public/Documents" to change the working directory
* "NAME toRename.txt" to specify the file to rename
* "TOBE renamed.txt" to rename the file
* Use "LIST F" to prove that this has happened

## DONE
* At any time, use this command to safely close the connection
* Server will repond with a "+" and then the connection will be closed

## RETR
* Create a file called toSend.txt in the C:/Users/Public/Public Documents directory, and write some text in it
* "USER 3" to login
* "CDIR C:/Users/Public/Documents" to change the working directory
* "RETR toSend.txt" to specify the file to be received by the client - the server will respond with the size of the file in bytes
* "SEND" will cause the file to be sent, or:
* "STOP" will abort the operation
* Check the project folder to see that the file has been copied to there

## STOR
* Create a file called toSend.txt in the project folder, and write some text in it
* "USER 3" to login
* "CDIR C:/Users/Public/Documents" to change the working directory
* "STOR NEW toSend.txt" to specify the file to be sent by the client, this will create a new file
* "SIZE X" where X is the size of the file in bytes - right click on the toSend.txt and check its properties to find this
* The file will then be sent, check the C:/Users/Public/Public Documents directory to prove this
* Repeat the STOR and SIZE steps above, which will create a new generation of the toSend.txt file
* If OLD is used, then the toSend.txt file will be overwritten (if it exists)
* If APP is used, then the contents of the toSend.txt that is sent will be appended to the existing file on the server






