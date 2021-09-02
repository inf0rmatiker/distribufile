package networking;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public final static Integer PORT = 9001;
    private ServerSocket serverSocket = null;

    // --- Constructors ---

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

    // --- Public start method ---

    /**
     * Starts the server. This method continues to listen for incoming client connections until the program is terminated.
     * @throws IOException
     */
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

    // --- Private server utility methods ---

    /**
     * Checks to see if the client connection is valid and can be handled by the server. If the client connection 
     * is valid, the method will return true. If the client connection is invalid, the method will close the client connection
     * and return false.
     * 
     * @param socket The client socket to be checked.
     * @return True if the client connection is valid, false if the client connection is invalid.
     * @throws IOException Thrown if the client connection cannot be closed.
     */
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

    /**
     * 
     * @param socket 
     * @throws IOException
     */
    private void handleResponse(Socket socket) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        String response = dataInputStream.readUTF();
        System.out.println("Server received message: " + response);
    }

    /**
     * Simply closes the socket connetion that is provided.
     * @param socket The socket connection to be closed.
     * @throws IOException Thrown if the socket connection cannot be closed.
     */
    private void closeConnection(Socket socket) throws IOException {
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}