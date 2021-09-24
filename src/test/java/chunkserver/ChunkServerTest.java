package chunkserver;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkServerTest {


    private static String getTestResourcesPath() {
        String path = "src/test/resources";
        File file = new File(path);
        return file.getAbsolutePath();
    }


    @Test
    public void testDiscoverChunksNoChunks() {
        Chunk.setChunkDir(getTestResourcesPath());
        ChunkServer chunkServer = new ChunkServer("localhost", 9000);
        List<String> actual = chunkServer.discoverChunks();
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testDiscoverChunksSomeChunks() {
        Chunk.setChunkDir(getTestResourcesPath());
        ChunkServer chunkServer = new ChunkServer("localhost", 9000);
        try {

            // Create some dummy chunk files for the discovery mechanism to pick up
            String[] dummyFilenames = {
                    getTestResourcesPath() + "/test_file1.data_chunk3",
                    getTestResourcesPath() + "/test_directory/test_file2.data_chunk1"
            };
            File[] dummyFiles = new File[2];
            for (int i = 0; i < 2; i++) {
                dummyFiles[i] = new File(dummyFilenames[i]);
                assertTrue(dummyFiles[i].createNewFile());
            }

            assertEquals(2, chunkServer.discoverChunks().size());

            // Clean up dummy chunk files
            for (File dummyFile: dummyFiles) {
                assertTrue(dummyFile.delete());
            }

        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
