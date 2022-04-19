import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;    //represents user

    public Client(Socket socket, String username) { //constructor
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter((socket.getOutputStream())));   //IO from client
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);    //closes everything
        }
    }

    public void sendMessage() { //sends message from client to client handler through server
        try {
            bufferedWriter.write(username); //send username first (client identifier)
            bufferedWriter.newLine();
            bufferedWriter.flush();

            /*
            LOGIN LOOP
            continue to next loop for shutdown, logout, solve, list, send message
            */
            Scanner scanner = new Scanner(System.in);   //get input from console

            String str;
            int flag = 0;

            while(true) {   //login loop
                System.out.println("Enter a command");
                str = scanner.nextLine();   //take console input and send to server

                if (str.contains("LOGIN")) {    //LOGIN if credentials match file
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
                if (str.contains("MESSAGE")) {  //send message to user
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
                else if (str.contains("SHUTDOWN")) { //SHUTDOWN server
                    System.out.println("Server shutting down");
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    flag = 1;
                    break;
                }
                else if (str.contains("LOGOUT")) {   //close client
                    System.out.println("Client shutting down");
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    flag = 1;
                    break;
                }
                else {  //send to server
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
            if (flag == 1) {    //close connection and leave function
                System.out.println("200 OK");
                closeEverything(socket, bufferedReader, bufferedWriter);
                return;
            }           
            
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);    //closes everything
        }
    }

    public void listenForMessage() {    //listens for messages from server (broadcasted messages)   each client has separate thread listening
        new Thread(new Runnable() { //use thread to circumvent block (program not stuck listening)
            @Override
            public void run() { //runs on separate thread
                String msgFromGroup;

                while (socket.isConnected()) {
                    try {
                        msgFromGroup = bufferedReader.readLine();   //read from server
                        System.out.println(msgFromGroup);   //output from server
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);    //closes everything
                    }
                }
            }
        
        }).start(); //starts object
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {  //closes everything
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

    public static void main (String[] args) throws IOException {    //runs everything
        int SERVER_PORT = 5352;
        Scanner scanner = new Scanner(System.in);   //take input from console
        System.out.println("Enter your username: ");
        String username = scanner.nextLine() + "\n";   //read username from client
        Socket socket = new Socket ("localhost", SERVER_PORT); //create connection to server
        Client client = new Client(socket, username);   //create new clients
        client.listenForMessage();  //listens for messages on a separate thread
        client.sendMessage();   //edit to implement login and other functions . . .
    }
}
