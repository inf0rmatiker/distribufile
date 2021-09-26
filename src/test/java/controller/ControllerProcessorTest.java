package controller;

import messaging.ChunkStoreRequest;
import messaging.HeartbeatMajor;
import messaging.HeartbeatMinor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import chunkserver.ChunkMetadata;

public class ControllerProcessorTest {

    @Test
    public void testConstructorAndGetter() {
        Controller controller = new Controller();
        ControllerProcessor processor = new ControllerProcessor(null, controller);
        assert processor.getController() != null;
    }

    @Test
    public void testProcessHeartBeatMajor() {
        Controller controller = new Controller();
        ControllerProcessor processor = new ControllerProcessor(null, controller);

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

        HeartbeatMajor heartbeatMajor = new HeartbeatMajor(testHostname, testIpAddr, testPort,
                testTotalChunksMaintained, testFreeSpaceAvailable, chunksMetadata);

        processor.process(heartbeatMajor);
    }

    @Test
    public void testProcessHeartbeatMinor() {
        Controller controller = new Controller();
        ControllerProcessor processor = new ControllerProcessor(null, controller);

        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        int testTotalChunks = 0;
        long testFreeSpace = 0L;
        List<ChunkMetadata> testNewChunks = new ArrayList<>();
        List<ChunkMetadata> testCorruptedFiles = new ArrayList<>();
        HeartbeatMinor heartbeatMinor = new HeartbeatMinor(testHostname, testIpAddr, testPort, testTotalChunks,
                testFreeSpace, testNewChunks, testCorruptedFiles);

        processor.process(heartbeatMinor);
    }

    @Test
    public void testProcessInvalidMessage() {
        Controller controller = new Controller();
        ControllerProcessor processor = new ControllerProcessor(null, controller);

        String testHostname = "shark";
        String testIpAddr = "129.82.45.138";
        int testPort = 9001;
        List<String> replicationChunkServers = new ArrayList<>(Arrays.asList("tuna", "bass"));
        String testAbsolutePath = "/path/to/my/file";
        int testSequence = 3;
        byte[] testChunkData = "test chunk data".getBytes();
        ChunkStoreRequest chunkStoreRequest = new ChunkStoreRequest(testHostname, testIpAddr, testPort,
                replicationChunkServers, testAbsolutePath, testSequence, testChunkData);

        processor.process(chunkStoreRequest);
    }

}
