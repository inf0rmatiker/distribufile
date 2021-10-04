package messaging;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ChunkReadRequestTest {

    @Test
    public void testMarshalToUnmarshal() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        String testAbsolutePath = "/path/to/my/file";
        int testSequence = 3;
        ChunkReadRequest a = new ChunkReadRequest(testHostname, testIpAddr, testPort, testAbsolutePath, testSequence);

        try {
            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(a.getMarshaledBytes());
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));
            dataInputStream.readInt(); // skip type

            // Create a new Message from a's marshaled bytes
            ChunkReadRequest b = new ChunkReadRequest(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(a, b);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
