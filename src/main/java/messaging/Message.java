package messaging;

import util.Host;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.UnknownHostException;

public class Message {

    public enum MessageType {
        HEARTBEAT_MINOR, HEARTBEAT_MAJOR
    }

    public MessageType type;
    public String hostname, ipAddress;
    public Integer port;
    public byte[] marshalledBytes;

    // --- Constructors ---

    public Message() throws UnknownHostException {
        this(MessageType.HEARTBEAT_MINOR); // default to type 0
    }

    public Message(MessageType type) throws UnknownHostException {
        this(type, Host.getHostname(), Host.getIpAddress(), 9001); // default to port 9001
    }

    public Message(MessageType type, String hostname, String ipAddress, Integer port) {
        this.type = type;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public Message(byte[] marshalledBytes) {
        this.marshalledBytes = marshalledBytes;
    }

    // --- Getters ---

    public MessageType getType() {
        return type;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public byte[] getMarshalledBytes() {
        return marshalledBytes;
    }

    // --- Common message utility functions ---

    /**
     * Marshals/packs the object header fields into the message's byte array representation.
     * The message header is represented as follows:
     * - message type (int 8 bytes)
     * - hostname length (int 8 bytes)
     * - hostname string (char[] n bytes)
     * - ip length (int 8 bytes)
     * - ip string (char[] n bytes)
     * - port (int 8 bytes)
     */
    public void marshalHeader() throws IOException {

        // Open DataInputStream for pushing data into byte array
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(this.marshalledBytes);
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

        // TODO: Implement

        // Close streams gracefully
        dataInputStream.close();
        byteInputStream.close();
    }

    /**
     * Converts an integer to a MessageType enum
     * @param type integer type
     * @return MessageType enum
     */
    public static MessageType typeFromInteger(int type) {
        switch (type) {
            case 0: return MessageType.HEARTBEAT_MINOR;
            case 1: return MessageType.HEARTBEAT_MAJOR;
            default: return null;
        }
    }
}
