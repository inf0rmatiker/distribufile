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
import static org.junit.jupiter.api.Assertions.fail;

public class ClientWriteRequestTest {

    @Test
    public void testMarshalToUnmarshal() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        String testAbsolutePath = "/path/to/my/file";
        int testSequence = 3;
        ClientWriteRequest a = new ClientWriteRequest(testHostname, testIpAddr, testPort,
                testAbsolutePath, testSequence);

        try {
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
            a.marshal(dataOutStream);
            a.collectByteStream(dataOutStream, byteOutStream);

            // Clean up output streams
            dataOutStream.close();
            byteOutStream.close();

            // Create a new Message from a's marshaled bytes
            ClientWriteRequest b = new ClientWriteRequest(a.getMarshaledBytes());

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
