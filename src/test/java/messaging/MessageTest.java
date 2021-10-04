package messaging;

import chunkserver.ChunkMetadata;
import controller.FileMetadata;
import messaging.Message;

import org.junit.jupiter.api.Test;
import messaging.Message.MessageType;

import java.io.*;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests the static methods of the abstract class Message.
 */
public class MessageTest {

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

    @Test
    public void testIntegerFromTypeValid() {
        MessageType[] types = new MessageType[]{MessageType.HEARTBEAT_MINOR, MessageType.HEARTBEAT_MAJOR};
        int[] expecteds = new int[]{0, 1};
        for (int i = 0; i < types.length; ++i) {
            int expected = expecteds[i];
            Integer actual = Message.integerFromType(types[i]);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testWriteStringByteLength() {
        String testString = "random string";
        try {

            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));

            Message.writeString(dataOutStream, testString);

            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            int expected = Integer.BYTES + testString.length();
            int actual = testBytes.length;
            assertEquals(expected, actual);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testReadStringNonempty() {
        String expected = "random string";

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            dataOutStream.writeInt(expected.length());
            dataOutStream.writeBytes(expected);
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(testBytes);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            String actual = Message.readString(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(expected, actual);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testReadStringEmpty() {
        String expected = "";

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            dataOutStream.writeInt(0);
            dataOutStream.writeBytes(expected);
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(testBytes);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            String actual = Message.readString(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(expected, actual);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testWriteStringArrayByteLength() {
        String[] testStrings = new String[]{
                "random string one",
                "random string two",
                "random string three"
        };

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));

            Message.writeStringArray(dataOutStream, testStrings);

            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            int expected = Integer.BYTES; // for array length
            for (String testString: testStrings) {
                expected += Integer.BYTES + testString.length();
            }

            int actual = testBytes.length;
            assertEquals(expected, actual);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testReadStringArrayNonempty() {
        String[] expecteds = new String[]{
                "random string one",
                "random string two",
                "random string three"
        };

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            dataOutStream.writeInt(expecteds.length);
            for (String expected: expecteds) {
                dataOutStream.writeInt(expected.length());
                dataOutStream.writeBytes(expected);
            }
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(testBytes);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            String[] actuals = Message.readStringArray(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertArrayEquals(expecteds, actuals);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testReadStringArrayEmpty() {
        String[] expecteds = new String[]{};

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            dataOutStream.writeInt(expecteds.length);
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(testBytes);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            String[] actuals = Message.readStringArray(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertArrayEquals(expecteds, actuals);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testWriteAndReadChunkMetadata() {
        Instant now = Instant.now();
        Timestamp tsNow = Timestamp.from(now);
        ChunkMetadata expected = new ChunkMetadata("/test_filepath", 0, 0, tsNow, 0);

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            Message.writeChunkMetadata(dataOutStream, expected);
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(testBytes);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            ChunkMetadata actual = Message.readChunkMetadata(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(expected, actual);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testWriteAndReadChunkMetadataList() {
        List<ChunkMetadata> expecteds = new ArrayList<>();
        Instant now = Instant.now();
        Timestamp tsNow = Timestamp.from(now);
        expecteds.add(new ChunkMetadata("/test_1_filepath", 1, 7, tsNow, 0));
        expecteds.add(new ChunkMetadata("/test_2_filepath", 1, 4, tsNow, 0));

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            Message.writeChunkMetadataList(dataOutStream, expecteds);
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(testBytes);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            List<ChunkMetadata> actuals = Message.readChunkMetadataList(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(expecteds, actuals);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testWriteAndReadStringList() {
        List<String> expecteds = new ArrayList<>(Arrays.asList("string_1", "string_2", "string_3"));

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            Message.writeStringList(dataOutStream, expecteds);
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(testBytes);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            List<String> actuals = Message.readStringList(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(expecteds, actuals);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testWriteAndReadFileMetadata() {

        String testAbsoluteFilePath = "/path/to/my/file.data";
        Vector<Set<String>> testChunkServerHostnames = new Vector<>();
        testChunkServerHostnames.add(new HashSet<>(Arrays.asList("penguin", "shrimp", "tuna")));
        testChunkServerHostnames.add(new HashSet<>(Arrays.asList("shark", "swordfish", "sole")));
        testChunkServerHostnames.add(new HashSet<>(Arrays.asList("sole", "shark", "penguin")));
        testChunkServerHostnames.add(new HashSet<>(Arrays.asList("shrimp", "tuna", "swordfish")));
        FileMetadata a = new FileMetadata(testAbsoluteFilePath, testChunkServerHostnames);

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            Message.writeFileMetadata(dataOutStream, a);
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(testBytes);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            FileMetadata b = Message.readFileMetadata(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(a, b);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
