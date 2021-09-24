package messaging;

import chunkserver.ChunkMetadata;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        List<ChunkMetadata> testNewChunks = new ArrayList<>();
        List<ChunkMetadata> testCorruptedFiles = new ArrayList<>();
        HeartbeatMinor a = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable, testNewChunks, testCorruptedFiles);

        try {
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
