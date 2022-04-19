import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;  //object to listen for connections

    public Server (ServerSocket serverSocket) { //constructor
        this.serverSocket = serverSocket;
    }

    public void startServer() { //method to start server
        
        try {
            
            while (!serverSocket.isClosed()) {  //server runs indefinitely
                
                Socket socket = serverSocket.accept();  //wait for client to connect
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket);    //responisble for multiple clients

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {

        }
    }

    public void closeServerSocket() {   //closes server socket
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        int SERVER_PORT = 5352;
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}