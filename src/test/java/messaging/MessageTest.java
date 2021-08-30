package messaging;

import org.junit.jupiter.api.Test;
import messaging.Message.MessageType;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;


public class MessageTest {

    @Test
    public void testDefaultConstructor() {
        try {
            Message message = new Message();
            MessageType expected = MessageType.HEARTBEAT_MINOR;
            MessageType actual = message.getType();
            assertEquals(expected, actual);
        } catch (UnknownHostException e) {
            fail("Caught UnknownHostException!");
        }
    }

    @Test
    public void testTypeArgsConstructor() {
        try {
            Message message = new Message(MessageType.HEARTBEAT_MAJOR);
            MessageType expected = MessageType.HEARTBEAT_MAJOR;
            MessageType actual = message.getType();
            assertEquals(expected, actual);
        } catch (UnknownHostException e) {
            fail("Caught UnknownHostException!");
        }
    }

    @Test
    public void testAllArgsConstructor() {
        Message message = new Message(MessageType.HEARTBEAT_MAJOR, "shark", "129.82.45.138", 9001);
        MessageType expectedType = MessageType.HEARTBEAT_MAJOR;
        MessageType actualType = message.getType();
        String expectedHostname = "shark";
        String actualHostname = message.getHostname();
        String expectedIpAddr = "129.82.45.138";
        String actualIpAddr = message.getIpAddress();
        Integer expectedPort = 9001;
        Integer actualPort = message.getPort();

        assertEquals(expectedType, actualType);
        assertEquals(expectedHostname, actualHostname);
        assertEquals(expectedIpAddr, actualIpAddr);
        assertEquals(expectedPort, actualPort);
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
