package networking;

import java.io.*;
import java.net.*;

import messaging.Message;


public abstract class Client {

    /**
     * Sends a Message to the Socket connection that has been opened.
     * @param hostname the String host name we are opening a Socket to
     * @param port the Integer port number we are opening a Socket to
     * @param message The Message to be sent.
     * @return The Socket we sent the Message on, and on which a response may be expected
     */
    public Socket sendMessage(String hostname, Integer port, Message message) throws IOException {
        Socket clientSocket = new Socket(hostname, port);
        clientSocket.getOutputStream().write(message.getMarshaledBytes());
        return clientSocket;
    }

    public void closeConnection(Socket socket) throws IOException {
        socket.close();
    }

    public abstract void processResponse(Message message);

}
