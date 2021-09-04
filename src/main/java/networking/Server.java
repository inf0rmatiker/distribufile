package networking;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    public final static Integer PORT = 9001;
    private ServerSocket serverSocket;



    // --- Constructors ---

    public Server() {
        this(PORT);
    }

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port, 10);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + port);
            e.printStackTrace();
        }
    }

    // --- Getters ---

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    // --- Public start method ---

    /**
     * Starts the server. This method continues to listen for incoming client
     * connections until the program is terminated.
     * @Override
     */
    public void run() {
        while (!serverSocket.isClosed()) {

            try(Socket clientSocket = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream())) {
                System.out.println("🚀Server started🚀");
                while (!clientSocket.isClosed()) {
                    handleResponse(dataInputStream);
                }

            } catch (IOException e) {
                System.out.println("Client disconnected.");
            }
        }

    }

    // --- Private server utility methods ---

    /**
     * 
     * @param dataInputStream inputStream from client Socket
     * @throws IOException when inputStream cannot read
     */
    private void handleResponse(DataInputStream dataInputStream) throws IOException {
        System.out.println("Server received message: " + dataInputStream.readUTF());
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.run();
    }

}