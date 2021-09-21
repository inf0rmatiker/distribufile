package controller.dataStructures;

import org.junit.jupiter.api.Test;

import chunkserver.ChunkMetadata;
import controller.dataStructures.ChunkServerMetadata;
import controller.dataStructures.FileMetadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static util.Constants.MB;

import java.util.Vector;

public class FileMetadataTest {

    @Test
    public void testFileArgsConstructor() {
        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data");
        assertEquals("/s/tmp/test.data", fileMetadata.absolutePath);
        assertEquals(0, fileMetadata.chunksServers.size());
    }

    @Test
    public void testFullArgsConstructor() {
        Vector<ChunkMetadata> chunks = new Vector<ChunkMetadata>() {
            {
                add(new ChunkMetadata("test.data", 4, 5 * MB));
            }
        };

        ChunkServerMetadata chunkServerMetadata = new ChunkServerMetadata("localhost", Long.valueOf(3 * MB),
                Integer.valueOf(5), chunks);

        Vector<Vector<ChunkServerMetadata>> chunksServers = new Vector<>();
        chunksServers.add(new Vector<>() {
            {
                add(chunkServerMetadata);
            }
        });

        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data", chunksServers);
        assertEquals("/s/tmp/test.data", fileMetadata.absolutePath);
        assertEquals(1, fileMetadata.chunksServers.size());
        assertEquals(1, fileMetadata.chunksServers.get(0).size());
        assertEquals(fileMetadata.chunksServers.get(0).get(0).hostname, chunkServerMetadata.hostname);
    }

    @Test
    public void testEquals() {
        FileMetadata fileMetadata = new FileMetadata("localhost");
        FileMetadata fileMetadata2 = new FileMetadata("localhost");
        String hostname = "localhost";
        assertEquals(fileMetadata, fileMetadata2); // two FileMetadata Objects
        assertEquals(fileMetadata, hostname); // one FileMetadata and one String. For .contains in Vector.
    }

}
