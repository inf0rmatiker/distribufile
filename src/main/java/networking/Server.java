package networking;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public final static Integer PORT = 9001;

    private ServerSocket serverSocket = null;

    public Server() {
        this(PORT);
    }

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port, 10);
            System.out.println("Server started");

        } catch (IOException e) {
            System.out.println("Could not listen on port: " + port);
        }
    }

    public void start() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            while (!socket.isClosed()) {
                if(validClientConnection(socket)) {
                    handleResponse(socket);
                }
            }
        }
    }

    private Boolean validClientConnection(Socket socket) throws IOException {
        if(socket.getInputStream().read() == -1) {
            System.out.println("Client disconnected...");
            System.out.println("Closing client connection...");
            closeConnection(socket);
            System.out.println("Client connection closed");
            return false;
        } else {
            return true;
        }
    }

    private void handleResponse(Socket socket) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        String response = dataInputStream.readUTF();
        System.out.println("Server received message: " + response);
    }

    private void closeConnection(Socket socket) throws IOException {
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}