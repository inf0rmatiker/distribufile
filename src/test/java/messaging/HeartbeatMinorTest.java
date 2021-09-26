package messaging;

import chunkserver.ChunkMetadata;
import messaging.Message.MessageType;
import org.junit.jupiter.api.Test;

import messaging.HeartbeatMinor;
import messaging.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the concrete implementation of the HeartbeatMinor class.
 */
public class HeartbeatMinorTest {

    @Test
    public void testTypeAllArgsConstructor() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        int testTotalChunks = 0;
        long testFreeSpace = 0L;
        List<ChunkMetadata> testNewChunks = new ArrayList<>();
        List<ChunkMetadata> testCorruptedFiles = new ArrayList<>();

        HeartbeatMinor message = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunks,
                testFreeSpace, testNewChunks, testCorruptedFiles);
        MessageType expectedType = MessageType.HEARTBEAT_MINOR;

        assertEquals(expectedType, message.getType());
        assertEquals(testHostname, message.getHostname());
        assertEquals(testIpAddr, message.getIpAddress());
        assertEquals(testPort, message.getPort());
        assertEquals(testTotalChunks, message.getTotalChunksMaintained());
        assertEquals(testFreeSpace, message.getFreeSpaceAvailable());
        assertEquals(testNewChunks, message.getNewlyAddedChunks());
        assertEquals(testCorruptedFiles, message.getCorruptedChunks());
    }

    @Test
    public void testMarshalByteLength() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        List<ChunkMetadata> testNewChunks = new ArrayList<>();
        List<ChunkMetadata> testCorruptedFiles = new ArrayList<>();
        HeartbeatMinor message = new HeartbeatMinor(testHostname, testIpAddr,
                9001, 0, 0L, testNewChunks, testCorruptedFiles);
        try {
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            message.marshal(dataOutStream);
            message.collectByteStream(dataOutStream, byteOutStream);

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            assertNotNull(message.getMarshaledBytes());

            // Calculate expected length
            int expectedByteLength = (4 * Integer.BYTES) + (testHostname.length() + testIpAddr.length());
            expectedByteLength += Integer.BYTES + Long.BYTES; // Heartbeat common stuff
            expectedByteLength += Integer.BYTES; // newly added chunks list length
            expectedByteLength += Integer.BYTES; // corrupted chunks list length

            int actualByteLength = message.getMarshaledBytes().length;
            assertEquals(expectedByteLength, actualByteLength);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testMarshalToUnmarshal() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        int testTotalChunksMaintained = 0;
        long testFreeSpaceAvailable = 0L;
        List<ChunkMetadata> testNewChunks = new ArrayList<>();
        testNewChunks.add(new ChunkMetadata("/path/to/my/file.data", 3, 35000));
        testNewChunks.add(new ChunkMetadata("/path/to/my/file2.data", 1, 39000));
        List<ChunkMetadata> testCorruptedFiles = new ArrayList<>();
        HeartbeatMinor a = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable, testNewChunks, testCorruptedFiles);

        try {
            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(a.getMarshaledBytes());
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));
            dataInputStream.readInt(); // skip type

            // Create a new Message from a's marshaled bytes
            HeartbeatMinor b = new HeartbeatMinor(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(a, b);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testGetType() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        int testTotalChunksMaintained = 0;
        long testFreeSpaceAvailable = 0L;
        List<ChunkMetadata> testNewChunks = new ArrayList<>();
        List<ChunkMetadata> testCorruptedFiles = new ArrayList<>();
        Message message = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable, testNewChunks, testCorruptedFiles);

        MessageType expected = MessageType.HEARTBEAT_MINOR;
        MessageType actual = message.getType();
        assertEquals(expected, actual);
    }
}
