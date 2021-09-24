package controller;

import chunkserver.ChunkMetadata;
import org.junit.jupiter.api.Test;

import java.util.Vector;

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
        Vector<ChunkMetadata> testChunkMetadata = new Vector<>();
        ChunkServerMetadata testCSM = new ChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                testNumChunksMaintained, testChunkMetadata);

        controller.updateChunkServerMetadata(testCSM);

        assertEquals(1, controller.getChunkServerMetadata().size());
    }

    @Test
    public void testUpdateChunkServerMetadataExistingCSM() {
        Controller controller = new Controller();
        assertTrue(controller.getChunkServerMetadata().isEmpty());

        String testHostname = "shark";
        Integer testNumChunksMaintained = 0;
        Long testFreeSpaceAvailable = 0L;
        Vector<ChunkMetadata> testChunkMetadata = new Vector<>();
        ChunkServerMetadata testCSM = new ChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                testNumChunksMaintained, testChunkMetadata);

        controller.updateChunkServerMetadata(testCSM);

        ChunkMetadata newChunkMetadata = new ChunkMetadata("/path/to/my/file.data", 3, 39000);
        Vector<ChunkMetadata> newTestChunkMetadata = new Vector<>();
        newTestChunkMetadata.add(newChunkMetadata);
        testCSM = new ChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                1, newTestChunkMetadata);

        controller.updateChunkServerMetadata(testCSM);

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
        Vector<ChunkMetadata> newTestChunkMetadata = new Vector<>();
        newTestChunkMetadata.add(chunkMetadata);
        ChunkServerMetadata testCSM = new ChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                1, newTestChunkMetadata);

        System.out.println(testCSM);

        controller.updateChunkServerMetadata(testCSM);
        assertEquals(1, controller.getChunkServerMetadata().size());

        testCSM = new ChunkServerMetadata(testHostname, testFreeSpaceAvailable,
                0, new Vector<>());

        controller.replaceChunkServerMetadata(testCSM);
        assertEquals(1, controller.getChunkServerMetadata().size());
        assertEquals(0, controller.getChunkServerMetadata().get("shark").totalChunksMaintained);
        assertEquals(0, controller.getChunkServerMetadata().get("shark").chunkMetadata.size());
    }
}
