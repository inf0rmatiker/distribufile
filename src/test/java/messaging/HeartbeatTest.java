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

public class HeartbeatTest {

    @Test
    public void testMarshalByteLength() {
        Message.MessageType testType = Message.MessageType.HEARTBEAT_MAJOR;
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        int testTotalChunksMaintained = 0;
        long testFreeSpaceAvailable = 0L;
        Heartbeat message = new Heartbeat(testType, testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable);
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
            int expectedByteLength = expectedHeaderByteLength + Integer.BYTES + Long.BYTES;
            int actualByteLength = message.getMarshaledBytes().length;
            assertEquals(expectedByteLength, actualByteLength);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testMarshalToUnmarshal() {
        Message.MessageType testType = Message.MessageType.HEARTBEAT_MAJOR;
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        int testTotalChunksMaintained = 0;
        long testFreeSpaceAvailable = 0L;
        Heartbeat a = new Heartbeat(testType, testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable);

        try {
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            a.marshal(dataOutStream);
            a.collectByteStream(dataOutStream, byteOutStream);

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Create a new Message from a's marshaled bytes
            Heartbeat b = new Heartbeat(a.getMarshaledBytes());

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
}
