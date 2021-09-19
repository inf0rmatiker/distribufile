package networking;

import java.io.*;
import java.net.*;

import messaging.Message;


public abstract class Client {

    /**
     * Opens a Socket to a hostname:port destination and sends a Message
     * @param hostname the String host name we are opening a Socket to
     * @param port the Integer port number we are opening a Socket to
     * @param message The Message to be sent, must have been previously marshaled
     * @return The Socket we sent the Message on, and on which a response may be expected
     */
    public static Socket sendMessage(String hostname, Integer port, Message message) throws IOException {
        Socket clientSocket = new Socket(hostname, port);
        clientSocket.getOutputStream().write(message.getMarshaledBytes());
        return clientSocket;
    }

    public abstract void processResponse(Message message);

}
