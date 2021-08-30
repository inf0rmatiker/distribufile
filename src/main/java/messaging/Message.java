package messaging;

import util.Host;
import java.net.UnknownHostException;

public class Message {

    public enum MessageType {
        HEARTBEAT_MINOR, HEARTBEAT_MAJOR
    }

    public final MessageType type;
    public final String hostname, ipAddress;
    public final Integer port;

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
