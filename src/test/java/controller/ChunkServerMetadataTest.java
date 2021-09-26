package controller;

import org.junit.jupiter.api.Test;

import chunkserver.ChunkMetadata;
import controller.ChunkServerMetadata;

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

        ChunkServerMetadata chunkServerMetadata = new ChunkServerMetadata("localhost", 3L * MB,
                5, chunks);
        assertEquals("localhost", chunkServerMetadata.hostname);
        assertEquals(3L * MB, chunkServerMetadata.freeSpaceAvailable);
        assertEquals(5, chunkServerMetadata.totalChunksMaintained);
        assertEquals(chunks, chunkServerMetadata.chunkMetadata);
    }

    @Test
    public void testEquals() {
        Vector<ChunkMetadata> chunks = new Vector<ChunkMetadata>() {
            {
                add(new ChunkMetadata("test.data", 4, 5 * MB));
            }
        };

        ChunkServerMetadata chunkServerMetadata = new ChunkServerMetadata("localhost", 3L * MB,
                5, chunks);
        ChunkServerMetadata chunkServerMetadata2 = new ChunkServerMetadata("localhost", 3L * MB,
                5, chunks);
        assertEquals(chunkServerMetadata, chunkServerMetadata2);
    }

}
