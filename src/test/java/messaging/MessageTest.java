package messaging;

import org.junit.jupiter.api.Test;
import messaging.Message.MessageType;

import java.io.*;
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
    public void testMarshalByteLength() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        Message message = new Message(MessageType.HEARTBEAT_MAJOR, testHostname, testIpAddr, 9001);
        try {
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            message.marshal(dataOutStream);
            message.collectByteStream(dataOutStream, byteOutStream);

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            assertNotNull(message.getMarshaledBytes());
            int expectedHeaderByteLength = (4 * Integer.BYTES) + (testHostname.length() + testIpAddr.length());
            int actualHeaderByteLength = message.getMarshaledBytes().length;
            assertEquals(expectedHeaderByteLength, actualHeaderByteLength);
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
    public void testUnmarshal() {
        MessageType expectedType = MessageType.HEARTBEAT_MAJOR;
        String expectedHostname = "shark";
        String expectedIpAddr = "129.82.45.138";
        int expectedPort = 9001;

        try {
            // Build test byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            dataOutStream.writeInt(1);
            dataOutStream.writeInt(expectedHostname.length());
            dataOutStream.writeBytes(expectedHostname);
            dataOutStream.writeInt(expectedIpAddr.length());
            dataOutStream.writeBytes(expectedIpAddr);
            dataOutStream.writeInt(expectedPort);
            dataOutStream.flush();
            byte[] testBytes = byteOutStream.toByteArray();

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Create a new Message from the test bytes
            Message message = new Message(testBytes);

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(message.getMarshaledBytes());
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            message.unmarshal(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(expectedType, message.getType());
            assertEquals(expectedHostname, message.getHostname());
            assertEquals(expectedIpAddr, message.getIpAddress());
            assertEquals(expectedPort, message.getPort());
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testMarshalToUnmarshal() {
        MessageType testType = MessageType.HEARTBEAT_MAJOR;
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;

        Message a = new Message(testType, testHostname, testIpAddr, testPort);
        try {
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            a.marshal(dataOutStream);
            a.collectByteStream(dataOutStream, byteOutStream);

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Create a new Message from a's marshaled bytes
            Message b = new Message(a.getMarshaledBytes());

            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(b.getMarshaledBytes());
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));
            b.unmarshal(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(a, b);
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
}
