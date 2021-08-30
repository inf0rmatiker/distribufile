package messaging;

import util.Host;

import java.io.*;
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
     * - message type (int 4 bytes)
     * - hostname length (int 4 bytes)
     * - hostname string (char[] n bytes)
     * - ip length (int 4 bytes)
     * - ip string (char[] n bytes)
     * - port (int 4 bytes)
     */
    public void marshalHeader() throws IOException {

        // Open DataOutputStream for pushing data into byte array
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));

        // Write header fields to data output stream
        dataOutStream.writeInt(integerFromType(this.type));
        dataOutStream.writeInt(this.hostname.length());
        dataOutStream.writeBytes(this.hostname);
        dataOutStream.writeInt(this.ipAddress.length());
        dataOutStream.writeBytes(this.ipAddress);
        dataOutStream.writeInt(this.port);

        // Flush DataOutputStream to the ByteArrayOutputStream, then collect bytes
        dataOutStream.flush();
        this.marshalledBytes = byteOutStream.toByteArray();

        // Close streams gracefully
        dataOutStream.close();
        byteOutStream.close();
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

    /**
     * Converts a MessageType enum to an integer
     * @param type MessageType enum
     * @return integer type
     */
    public static Integer integerFromType(MessageType type) {
        switch (type) {
            case HEARTBEAT_MINOR: return 0;
            case HEARTBEAT_MAJOR: return 1;
            default: return -1;
        }
    }
}
