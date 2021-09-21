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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HeartbeatMajorTest {

    @Test
    public void testMarshalToUnmarshal() {
        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        int testTotalChunksMaintained = 0;
        long testFreeSpaceAvailable = 0L;
        List<ChunkMetadata> chunksMetadata = new ArrayList<>();
        Instant now = Instant.now();
        Timestamp tsNow = Timestamp.from(now);
        chunksMetadata.add(new ChunkMetadata("/test_1_filepath", 1, 7, tsNow, 0));
        chunksMetadata.add(new ChunkMetadata("/test_2_filepath", 1, 4, tsNow, 0));

        HeartbeatMajor a = new HeartbeatMajor(testHostname, testIpAddr, testPort, testTotalChunksMaintained,
                testFreeSpaceAvailable, chunksMetadata);

        try {
            // Init test input stream
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(a.getMarshaledBytes());
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));
            dataInputStream.readInt(); // skip type

            // Create a new Message from a's marshaled bytes
            HeartbeatMajor b = new HeartbeatMajor(dataInputStream);

            // Clean up input streams
            dataInputStream.close();
            byteInputStream.close();

            assertEquals(a, b);
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
