package messaging;

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
        String[] testNewChunks = new String[]{};
        String[] testCorruptedFiles = new String[]{};

        HeartbeatMinor message = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunks,
                testFreeSpace, testNewChunks, testCorruptedFiles);
        MessageType expectedType = MessageType.HEARTBEAT_MINOR;

        assertEquals(expectedType, message.getType());
        assertEquals(testHostname, message.getHostname());
        assertEquals(testIpAddr, message.getIpAddress());
        assertEquals(testPort, message.getPort());
        assertEquals(testTotalChunks, message.getTotalChunksMaintained());
        assertEquals(testFreeSpace, message.getFreeSpaceAvailable());
        assertArrayEquals(testNewChunks, message.getNewlyAddedChunks());
        assertArrayEquals(testCorruptedFiles, message.getCorruptedFiles());
    }

    @Test
    public void testMarshalByteLength() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        String[] testNewlyAddedChunks = new String[]{"test chunk 1", "test chunk 5"};
        String[] testCorruptedFiles = new String[]{"test file 3", "test file 7"};
        HeartbeatMinor message = new HeartbeatMinor(testHostname, testIpAddr,
                9001, 0, 0L, testNewlyAddedChunks, testCorruptedFiles);
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
            expectedByteLength += Integer.BYTES; // newly added chunks array length
            for (String newlyAddedChunk: testNewlyAddedChunks) {
                expectedByteLength += Integer.BYTES + newlyAddedChunk.length();
            }
            expectedByteLength += Integer.BYTES; // corrupted files array length
            for (String corruptedFile: testCorruptedFiles) {
                expectedByteLength += Integer.BYTES + corruptedFile.length();
            }

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
        String[] testNewlyAddedChunks = new String[]{"test chunk 1", "test chunk 5"};
        String[] testCorruptedFiles = new String[]{"test file 3", "test file 7"};
        HeartbeatMinor a = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable, testNewlyAddedChunks, testCorruptedFiles);

        try {
            a.marshal();

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
        String[] testNewlyAddedChunks = new String[]{"test chunk 1", "test chunk 5"};
        String[] testCorruptedFiles = new String[]{"test file 3", "test file 7"};
        Message message = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable, testNewlyAddedChunks, testCorruptedFiles);

        MessageType expected = MessageType.HEARTBEAT_MINOR;
        MessageType actual = message.getType();
        assertEquals(expected, actual);
    }
}
