package networking;

import messaging.Message;
import messaging.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Processes Messages from a received Socket connection.
 * Contains common functions and abstractions, including a reference
 * to the Socket from which the Message is arriving and to which
 * we may need to respond. Runs as its own Thread, one thread per
 * current connection.
 */
public abstract class Processor implements Runnable {

    public static Logger log = LoggerFactory.getLogger(Processor.class);

    // The Socket connection containing the Message to process, and to which we respond
    public Socket socket;

    /**
     * Executed by Thread.start() as its own Thread; invokes processMessage() with a fully constructed
     * and unmarshaled Message from the MessageFactory.
     */
    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(this.socket.getInputStream());
            Message message = MessageFactory.getInstance().createMessage(dataInputStream);
            process(message);
        } catch (IOException e) {
            log.error("Caught IOException!");
        }
    }

    /**
     * Sends a Message response on an already-established Socket connection.
     * @param socket The Socket that has previously been established.
     * @param message The Message containing the response.
     */
    public static void sendResponse(Socket socket, Message message) {
        log.info("Sending {} response", message.getType());
        if (socket != null && socket.isConnected()) {
            try {
                socket.getOutputStream().write(message.getMarshaledBytes());
            } catch (IOException e) {
                log.error("Failed to send response Message {}: {}", message.getType(), e.getMessage());
            }
        } else {
            log.warn("Socket is null or has been disconnected; aborting {} response", message.getType());
        }
    }

    /**
     * Fully processes a request Message, responding to the Socket member if necessary.
     * Abstract and implemented by a concrete subclass.
     * @param message Message received over the Socket.
     */
    public abstract void process(Message message);

    /**
     * Helper function for launching our run() function as its own Thread.
     */
    public void launchAsThread() {
        Thread processor = new Thread(this, "Processor Thread");
        processor.start();
    }
}
