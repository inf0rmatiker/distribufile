package messaging;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ChunkStoreRequestTest {

    @Test
    public void testMarshalToUnmarshal() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        List<String> replicationChunkServers = new ArrayList<>(Arrays.asList("tuna", "bass"));
        String testAbsolutePath = "/path/to/my/file";
        int testSequence = 3;
        byte[] testChunkData = "test chunk data".getBytes();
        ChunkStoreRequest a = new ChunkStoreRequest(testHostname, testIpAddr, testPort, replicationChunkServers,
                testAbsolutePath, testSequence, testChunkData);


        try {
            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(a.getMarshaledBytes());
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));
            dataInputStream.readInt(); // skip type

            // Create a new Message from a's marshaled bytes
            ChunkStoreRequest b = new ChunkStoreRequest(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(a, b);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
