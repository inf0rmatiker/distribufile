package messaging;

import org.junit.jupiter.api.Test;
import messaging.Message.MessageType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class MessageTest {

    @Test
    public void testDefaultConstructor() {
        Message message = new Message();
        MessageType expected = MessageType.HEARTBEAT_MINOR;
        MessageType actual = message.getType();
        assertEquals(expected, actual);
    }

    @Test
    public void testArgsConstructor() {
        Message message = new Message(MessageType.HEARTBEAT_MAJOR);
        MessageType expected = MessageType.HEARTBEAT_MAJOR;
        MessageType actual = message.getType();
        assertEquals(expected, actual);
    }

    @Test
    public void testTypeFromIntegerValid() {
        int[] types = new int[]{0, 1};
        MessageType[] expecteds = new MessageType[]{MessageType.HEARTBEAT_MINOR, MessageType.HEARTBEAT_MAJOR};
        for (int i = 0; i < types.length; ++i) {
            MessageType expected = expecteds[i];
            MessageType actual = Message.typeFromInteger(types[i]);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testTypeFromIntegerInvalid() {
        assertNull(Message.typeFromInteger(-1));
        assertNull(Message.typeFromInteger(MessageType.values().length));
    }

}
