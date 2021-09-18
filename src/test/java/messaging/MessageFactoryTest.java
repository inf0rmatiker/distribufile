package messaging;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class MessageFactoryTest {

    @Test
    public void testGetInstance() {
        MessageFactory messageFactory = MessageFactory.getInstance();
        assertNotNull(messageFactory);
    }

    @Test
    public void testCreateMessage() {
        MessageFactory messageFactory = MessageFactory.getInstance();

        // Create HeartbeatMinor Message
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        int testTotalChunksMaintained = 0;
        long testFreeSpaceAvailable = 0L;
        String[] testNewlyAddedChunks = new String[]{"test chunk 1", "test chunk 5"};
        String[] testCorruptedFiles = new String[]{"test file 3", "test file 7"};
        HeartbeatMinor a = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable, testNewlyAddedChunks, testCorruptedFiles);

        try {
            a.marshal();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(a.getMarshaledBytes());
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            Message createdMessage = messageFactory.createMessage(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            Message.MessageType expected = Message.MessageType.HEARTBEAT_MINOR;
            Message.MessageType actual = createdMessage.getType();
            assertEquals(expected, actual);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
