package networking;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import chunkserver.ChunkIntegrity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

/**
 * Listens on a port and accepts incoming client connections to the ServerSocket, which generates a Socket object.
 * Once a connection is received/accepted and its Socket has been captured, a Processor is spawned off as a Thread
 * to process any incoming Message from the captured Socket, and we immediately return to listening for new connections.
 */
public abstract class Server implements Runnable {

    public static Logger log = LoggerFactory.getLogger(Server.class);

    public ServerSocket serverSocket;
    public Integer port;

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Integer getPort() {
        return port;
    }

    /**
     * Binds our ServerSocket to the specified port. If unable to bind, exits the program with error code 1.
     * @param port Integer port to which we are attempting to bind.
     */
    public void bindToPort(Integer port) {
        try {
            this.serverSocket = new ServerSocket(port, 10);
        } catch (IOException e) {
            log.error("Could not listen on port {}", port);
            e.printStackTrace();
        }

        if (!this.serverSocket.isBound()) {
            log.error("ServerSocket unable to bind to port {}", port);
            System.exit(1);
        }
        log.info("ServerSocket successfully bound to port {}", port);
        this.port = port;
    }

    /**
     * Accepts client connections to ServerSocket in the form of a Socket, which is then passed to a new
     * connection-handling thread. Finally, goes back to listening for more connections.
     */
    public void acceptConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept(); // blocking call, waits for connection
                log.info("Received client connection from host: {}, port: {}",
                        clientSocket.getInetAddress().getHostName(), clientSocket.getPort());

                this.processConnection(clientSocket); // consume/process incoming message on new thread
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            }
        }
    }

    /**
     * Helper function for launching our run() function as its own Thread.
     */
    public void launchAsThread() {
        Thread server = new Thread(this, "Server Thread");
        server.start();
    }

    /**
     * See class description; this is self-explanatory.
     */
    @Override
    public void run() {
        acceptConnections();
    }

    /**
     * Processes a captured Socket from an incoming connection. Implemented by a concrete subclass.
     * @param clientSocket Socket captured from the incoming connection.
     */
    public abstract void processConnection(Socket clientSocket);

}