package distribufile.messaging;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Singleton class to instantiate concrete Message instances
 * from a byte array.
 */
public class MessageFactory {

    private static MessageFactory singletonInstance = null;

    /**
     * Note: this constructor can only be called from within the class.
     */
    private MessageFactory() {}

    /**
     * Gets the singleton instance, instantiating it if it has not been already.
     * @return Singleton MessageFactory instance.
     */
    public static MessageFactory getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new MessageFactory();
        }
        return singletonInstance;
    }

    /**
     * Creates and returns a concrete Message subclass instance from the integer type specified by the byte message.
     * @param marshaledBytes byte[] containing the message
     * @return A concrete Message subclass instance
     * @throws IOException
     */
    public Message createMessage(byte[] marshaledBytes) throws IOException {

        // Read the first integer of the marshaledBytes
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream dataInStream = new DataInputStream(new BufferedInputStream(byteInStream));
        int integerType = dataInStream.readInt();
        dataInStream.close();
        byteInStream.close();

        // Create concrete Message using type in byte message
        Message.MessageType type = Message.typeFromInteger(integerType);
        if (type != null) {
            switch (type) {
                case HEARTBEAT_MINOR: return new HeartbeatMinor(marshaledBytes);
                case HEARTBEAT_MAJOR: return new HeartbeatMajor(marshaledBytes);
                default: return null;
            }
        } else {
            throw new IOException("Unable to determine MessageType for integer " + integerType);
        }
    }
}
