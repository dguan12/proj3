import java.net.Socket;
import java.util.ArrayList;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ClientHandler implements Runnable {    //client implemented by separate thread
    
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();  //array to hold clients
    private Socket socket;  //establish connection between client and server
    private BufferedReader bufferedReader;  //read data from client
    private BufferedWriter bufferedWriter;  //send data to client
    private String clientUsername;  //represents each client
    
    public ClientHandler(Socket socket) {   //constructor
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); //gets output from client
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));   //gets input from client
            this.clientUsername = bufferedReader.readLine();    //read username of client
            clientHandlers.add(this);   //add client to arraylist
            broadcastMessage("SERVER: " + clientUsername + " has entered");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);    //close everything
        }
    }

    static String user;

    public static String login(String str) throws FileNotFoundException {
        File file = new File("/Users/David/java/Server2/logins.txt");   //open file
        Scanner scan = new Scanner(file);   //used to read file
        str = str.substring(5); //remove LOGIN
        str = str.trim();   //remove surrounding spaces
        String temp = null;
        while(scan.hasNextLine()) { //scan file if there are still lines to read
            if (str.equals(scan.nextLine().trim())) {   //if str entered is in the file do this
                temp = str;
            }
        }
        scan.close();
        return temp;    //if user and pass are in file, function will not return null
    }

    public static void create(String input) throws IOException {
        String index = user;
        index = index.substring(0, index.indexOf(' ')); //only get first word
        File file = new File("/Users/David/java/Server2/" + index + "_solutions.txt");
        FileWriter fw = new FileWriter("/Users/David/java/Server2/" + index + "_solutions.txt", true);  //creates new file
        if (file.length() == 0) {   //if file is empty
            fw.write(index + System.lineSeparator());
        }
        fw.write(input + System.lineSeparator());   //write to file
        fw.close();

    }

    //function to print file contents, takes user as argument
    public static void printFile(String name) throws FileNotFoundException {
        
        String path = "/Users/David/java/server2/" + name + "_solutions.txt";
        Scanner input = new Scanner(new File(path));

        while (input.hasNextLine()) {
            System.out.println(input.nextLine());
        }
    }
    //print taking file path as argument
    public static void print (String path) throws FileNotFoundException {
        Scanner input = new Scanner(new File(path));

        while(input.hasNextLine()) {
            System.out.println(input.nextLine());
        }
    }
    
    @Override
    public void run() { //run on separate thread, listen for messages
        String messageFromClient;   //hold message from client

        int loginFlag = 0;
        int rootFlag = 0;
        int sFlag = 0;

        while(socket.isConnected()) {   //listen while a connection is still there
            
            try {
                messageFromClient = bufferedReader.readLine();  //blocks and waits for client to send username

                /*
                LOGIC to handle client inputs for different functions
                */

                String str = "";

                while (true) {  //runs program loop
                    str = bufferedReader.readLine();    //reads line from client
                    if (str.contains("LOGIN")) {    //logic to handle login from client
                        user = login(str);
                        if (user != null) {
                            System.out.println("SUCCESS");
                            //sendOne("SUCCESS");
                            if (user.contains("root")) {    //if user is root, set root flag for root operations
                                rootFlag = 1;
                            }
                            loginFlag = 1;
                            break;
                        }
                        else {
                            System.out.println("FAILURE : Please provide correct username and password.  Try again.");
                            //sendOne("FAILURE : Please provide correct username and password.  Try again.");
                        }
                    }
                    else if (str.contains("MESSAGE")) {
                        str = str.substring(7); //remove MESSAGE
                        str = str.trim();   //remove surrounding spaces
                        String messageToSend = "TEST";
                        String index = "";
                        String temp;
                        temp = str;

                        if (str.contains("-all")) { //handles messages to be broadcasted
                            //check if user is root user
                            if (rootFlag == 1) {
                                messageToSend = temp.substring(5);   //remove -all
                                broadcastMessageRoot(messageToSend);
                            }
                            else {
                                System.out.print("Error: not root user");
                            }
                        }
                        else {  //only send to one other user
                            index = temp.substring(0, temp.indexOf(' ')); //get username
                            messageToSend = temp.substring(index.length());
                            sendOne(messageToSend, index);
                        }
                        
                    }
                    else if (str.contains("SOLVE")) {
                        String temp = null;
                        if (loginFlag != 0) {   //check if user is logged in
                            if (str.contains("-c")) {
                                System.out.println("contains -c");
                                int counter = 0;
                                for (int i = 0; i < str.length(); i++) {    //count spaces in str
                                    temp = str;
                                    if (temp.charAt(i) == ' ') {
                                        counter++;
                                    }
                                }
                                //if there is another char after -c, solve
                                if (counter == 2) {
                                    double cir = 0;
                                    double area = 0;
                                    int parse = 0;
                                    String last = temp.substring(temp.lastIndexOf(" ") + 1);
                                    //solve for circumference and diameter
                                    parse = Integer.parseInt(last);
                                    System.out.println("Radius: " + parse);
                                    area = parse * parse * 3.14;
                                    cir = 2 * 3.14 * parse;
                                    String output = "Circle's circumference is " + cir + " and area is " + area;
                                    System.out.println(output);
                                    output = "radius " + parse + ": " + output;
                                 create(output);
                                }
                                else {
                                    String output = "Error: no radius found";
                                    System.out.println(output);
                                 create(output);
                                }
                            }
                            else if (str.contains("-r")) {
                                System.out.println("Contains -r");
                                //used to count spaces in the string to check for num to calc
                                int counter = 0;
                                //count spaces in str
                                for (int i = 0; i < str.length(); i++) {
                                    temp = str;
                                    if (temp.charAt(i) == ' ') {
                                        counter++;
                                    }
                                }
                                //if only one side is entered, square
                                if (counter == 2) {
                                    double per = 0;
                                    double area = 0;
                                    int parse = 0;
                                    String last = temp.substring(temp.lastIndexOf(" ") + 1);
                                    //solve for perimeter and area
                                    parse = Integer.parseInt(last);
                                    area = parse * parse;
                                    per = 4 * parse;
                                    String output = "Rectangle's perimeter is " + per + " and area is " + area;
                                    System.out.println(output);
                                    output = "side " + parse + ": " + output;
                                 create(output);
                                }
                                //rectangle computation
                                else if (counter == 3) {
                                    double per = 0;
                                    double area = 0;
                                    int parse = 0;
                                    double length = 0;
                                    double width = 0;
                                    //get one of the sides
                                    String last = temp.substring(temp.lastIndexOf(" ") + 1);
                                    parse = Integer.parseInt(last);
                                    width = parse;
                                    //get the other side
                                    temp = str;
                                    last = temp.substring(9,11);
                                    //if temp was only 1 digit
                                    if (last.contains(" ")) {
                                        last = last.trim();
                                    }
                                    parse = Integer.parseInt(last);
                                    length = parse;
                                    //solve for perimeter and area
                                    area = length * width;
                                    per = 2 * (length + width);
                                    String output = "Rectangle's perimeter is " + per + " and area is " + area;
                                    System.out.println(output);
                                    output = "sides " + length + " " + width + " " + output;
                                 create(output);
                                }
                                else {
                                    String output = "Error: No sides found";
                                    System.out.println(output);
                                 create(output);
                                }
                            }
                            else {
                                String output = "Error: enter -c or -r";
                                System.out.println(output);
                             create(output);
                            }
                        }
                        else {
                            System.out.println("Login first");
                        }
                    }
                    else if (str.contains("LIST")) {
                        //check if user wants to view all solutions
                        if (str.contains("-all")) {
                            //check if user is root user
                            if (user.contains("root")) {
                                //used to access to directory where all sol files stored
                                File dirPath = new File ("/Users/David/java/server2");
                                //list containing all files in directory
                                File fileList[] = dirPath.listFiles();
                                //iterate through list of files in directory
                                for (File file : fileList) {
                                    //if file contains a username print out file
                                    if ((file.getName()).contains("solutions")) {
                                        //prints content of files that have solutions in it
                                        print(file.getAbsolutePath());
                                    }
                                }
                            }
                            else {
                                System.out.print("Error: you are not the root user");
                            }
                        }
                        //regular list operation (print current user's txt file)
                        else {
                            String sub = user.substring(0, user.indexOf(' '));
                            //System.out.println(sub);
                            printFile(sub);
                        }
                    }
                    else if (str.equals("SHUTDOWN")) {
                        System.out.println("Server shutting down");
                        sFlag = 1;
                        break;
                    }
                    else {
                        System.out.println("300 Invalid command");
                    }
                }
                
                broadcastMessage(messageFromClient);    //possible remove/change to different send (not broadcast)
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);    //close everything
                break;  // leave loop
            }

            if (sFlag == 1) {
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {    //send message to every client that is in list
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) { //do not send message to self (same client)
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);    //close everything
            }
        }
    }

    public void broadcastMessageRoot(String messageToSend) {    //send message to every client that is in list
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) { //do not send message to self (same client)
                    clientHandler.bufferedWriter.write("Message from root: " + messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);    //close everything
            }
        }
    }

    public void sendOne(String messageToSend, String username) {    //send a message to a designated user
        int flag = 0;
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.clientUsername.equals(username)) {  //send message only to username
                    clientHandler.bufferedWriter.write(username + ": " + messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                    flag = 1;
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);    //close everything
            }
        }
        if (flag == 0) {
            //System.out.println("Client username: " + clientUsername + " username: " + username);  testing
            System.out.println(username + " is not logged in");
            System.out.println("Informing client.");
            try {
                bufferedWriter.write("User: " + username + " is not logged in.");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
    }

    public ClientHandler(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public void removeClientHandler() { //removes client from arraylist
        clientHandlers.remove(this);
        broadcastMessage("Server: " + clientUsername + " has left");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {  //closes all connections
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }
}
