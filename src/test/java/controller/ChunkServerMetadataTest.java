package controller;

import org.junit.jupiter.api.Test;

import chunkserver.ChunkMetadata;
import controller.dataStructures.ChunkServerMetadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static util.Constants.MB;

import java.util.Vector;

public class ChunkServerMetadataTest {

    @Test
    public void testConstructor() {
        Vector<ChunkMetadata> chunks = new Vector<ChunkMetadata>() {
            {
                add(new ChunkMetadata("test.data", 4, 5 * MB));
            }
        };

        ChunkServerMetadata chunkServerMetadata = new ChunkServerMetadata("localhost", Long.valueOf(3 * MB),
                Integer.valueOf(5), chunks);
        assertEquals("localhost", chunkServerMetadata.hostname);
        assertEquals(Long.valueOf(3 * MB), chunkServerMetadata.freeSpaceAvailable);
        assertEquals(Integer.valueOf(5), chunkServerMetadata.totalChunksMaintained);
        assertEquals(chunks, chunkServerMetadata.chunkMetadata);
    }

    @Test
    public void testEquals() {
        Vector<ChunkMetadata> chunks = new Vector<ChunkMetadata>() {
            {
                add(new ChunkMetadata("test.data", 4, 5 * MB));
            }
        };

        ChunkServerMetadata chunkServerMetadata = new ChunkServerMetadata("localhost", Long.valueOf(3 * MB),
                Integer.valueOf(5), chunks);
        ChunkServerMetadata chunkServerMetadata2 = new ChunkServerMetadata("localhost", Long.valueOf(3 * MB),
                Integer.valueOf(5), chunks);
        assertEquals(chunkServerMetadata, chunkServerMetadata2);
    }

}
