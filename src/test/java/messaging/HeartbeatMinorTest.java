package messaging;

import messaging.Message.MessageType;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;


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

}
