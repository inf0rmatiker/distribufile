package controller;

import chunkserver.ChunkMetadata;
import org.junit.jupiter.api.Test;
import util.Constants;


import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControllerTest {

    @Test
    public void testUpdateChunkServerMetadataNewCSM() {
        Controller controller = new Controller();
        assertTrue(controller.getChunkServerMetadata().isEmpty());

        String testHostname = "shark";
        Integer testNumChunksMaintained = 0;
        Long testFreeSpaceAvailable = 0L;
        List<ChunkMetadata> testChunkMetadata = new ArrayList<>();
        controller.updateChunkServerMetadata(testHostname, testFreeSpaceAvailable, testNumChunksMaintained, testChunkMetadata);

        assertEquals(1, controller.getChunkServerMetadata().size());
    }

    @Test
    public void testUpdateChunkServerMetadataExistingCSM() {
        Controller controller = new Controller();
        assertTrue(controller.getChunkServerMetadata().isEmpty());

        String testHostname = "shark";
        Integer testNumChunksMaintained = 0;
        Long testFreeSpaceAvailable = 0L;
        List<ChunkMetadata> testChunkMetadata = new ArrayList<>();

        controller.updateChunkServerMetadata(testHostname, testFreeSpaceAvailable, testNumChunksMaintained, testChunkMetadata);

        ChunkMetadata newChunkMetadata = new ChunkMetadata("/path/to/my/file.data", 3, 39000);
        Vector<ChunkMetadata> newTestChunkMetadata = new Vector<>();
        newTestChunkMetadata.add(newChunkMetadata);
        ChunkServerMetadata testCSM = new ChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                1, newTestChunkMetadata);

        controller.updateChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                1, newTestChunkMetadata);

        assertEquals(1, controller.getChunkServerMetadata().size());
        assertEquals(1, controller.getChunkServerMetadata().get("shark").totalChunksMaintained);
        assertEquals(1, controller.getChunkServerMetadata().get("shark").chunkMetadata.size());
    }

    @Test
    public void testReplaceChunkServerMetadataExistingCSM() {
        Controller controller = new Controller();
        assertTrue(controller.getChunkServerMetadata().isEmpty());

        String testHostname = "shark";
        Integer testNumChunksMaintained = 0;
        Long testFreeSpaceAvailable = 0L;
        ChunkMetadata chunkMetadata = new ChunkMetadata("/path/to/my/file.data", 3, 39000);
        List<ChunkMetadata> newTestChunkMetadata = new ArrayList<>();
        newTestChunkMetadata.add(chunkMetadata);
        ChunkServerMetadata testCSM = new ChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                1, new Vector<>(newTestChunkMetadata));

        System.out.println(testCSM);

        controller.updateChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                1, newTestChunkMetadata);
        assertEquals(1, controller.getChunkServerMetadata().size());

        testCSM = new ChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                0, new Vector<>());

        controller.replaceChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                0, new ArrayList<>());
        assertEquals(1, controller.getChunkServerMetadata().size());
        assertEquals(0, controller.getChunkServerMetadata().get("shark").totalChunksMaintained);
        assertEquals(0, controller.getChunkServerMetadata().get("shark").chunkMetadata.size());
    }

    @Test
    public void testSelectBestChunkServersForReplicas() {
        Controller controller = new Controller();

        Vector<ChunkMetadata> testMetadata = new Vector<>();
        Long testFreeBytes = 0L;

        controller.getChunkServerMetadata().put("a", new ChunkServerMetadata("a", testFreeBytes,
                0, testMetadata));
        controller.getChunkServerMetadata().put("b", new ChunkServerMetadata("b", testFreeBytes,
                4, testMetadata));
        controller.getChunkServerMetadata().put("c", new ChunkServerMetadata("c", testFreeBytes,
                2, testMetadata));
        controller.getChunkServerMetadata().put("d", new ChunkServerMetadata("d", testFreeBytes,
                6, testMetadata));
        controller.getChunkServerMetadata().put("e", new ChunkServerMetadata("e", testFreeBytes,
                3, testMetadata));
        controller.getChunkServerMetadata().put("f", new ChunkServerMetadata("f", testFreeBytes,
                15, testMetadata));

        Set<String> actual = controller.selectBestChunkServersForReplicas();
        assertEquals(Constants.CHUNK_REPLICATION, actual.size());

        Set<String> expected = new HashSet<>() {
            {
                add("a");
                add("c");
                add("e");
            }
        };
        assertTrue(actual.containsAll(expected));
    }
}
