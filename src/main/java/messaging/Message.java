package messaging;

public class Message {

    public enum MessageType {
        HEARTBEAT_MINOR, HEARTBEAT_MAJOR
    }

    public final MessageType type; // once set, this shouldn't change

    public Message() {
        this(MessageType.HEARTBEAT_MINOR); // default to type 0
    }

    public Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
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
