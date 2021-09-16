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
            processRequest(message);
        } catch (IOException e) {
            log.error("Caught IOException!");
        }
    }

    /**
     * Fully processes a request Message, responding to the Socket member if necessary.
     * Abstract and implemented by a concrete subclass.
     * @param message Message received over the Socket.
     */
    public abstract void processRequest(Message message);

    /**
     * Helper function for launching our run() function as its own Thread.
     */
    public void launchAsThread() {
        Thread processor = new Thread(this, "Processor Thread");
        processor.start();
    }
}
