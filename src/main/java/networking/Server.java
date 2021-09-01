package networking;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

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

        } catch(IOException e) {
            System.out.println("Could not listen on port: " + port);
        }    
    }


    public void start() throws IOException {
        while(true){
            Socket socket = acceptConnection();
            handleResponse(socket);
            closeConnection(socket);
        }
    }

    private Socket acceptConnection() throws IOException {
        return serverSocket.accept();
    }

    private void handleResponse(Socket socket) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Server received message: " + dataInputStream.readUTF());
        dataOutputStream.writeUTF(dataInputStream.readUTF());
    }

    private void closeConnection(Socket socket) throws IOException {
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server code");
        Server server = new Server();
        server.start();
    }
}