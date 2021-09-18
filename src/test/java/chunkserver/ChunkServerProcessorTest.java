package chunkserver;

import messaging.ChunkStoreRequest;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.Constants.CHUNK_SIZE;
import static util.Constants.KB;

public class ChunkServerProcessorTest {

    private static final String[] testFiles = {
            "input_0kb.data",
            "input_35kb.data",
            "input_64kb.data",
            "input_100kb.data",
            "input_680kb.data"
    };

    private static String getTestResourceAbsolutePath(String filename) {
        String path = "src/test/resources";
        File file = new File(path);
        String absolutePath = file.getAbsolutePath();
        return String.format("%s/%s", absolutePath, filename);
    }

    private static String getTestResourcesPath() {
        String path = "src/test/resources";
        File file = new File(path);
        return file.getAbsolutePath();
    }

    @Test
    public void testProcessChunkStoreRequestSavesFile() {
        List<String> testReplicationChunkServers = new ArrayList<>();
        String testFile = getTestResourceAbsolutePath(testFiles[1]);
        String previousChunkDir = Chunk.getChunkDir();
        Chunk.setChunkDir(getTestResourcesPath());
        try {
            // Read raw chunk data from 35KB file
            FileInputStream fileInputStream = new FileInputStream(testFile);
            BufferedInputStream reader = new BufferedInputStream(fileInputStream, CHUNK_SIZE);
            byte[] chunkData = new byte[35 * KB];
            int bytesRead = reader.read(chunkData, 0, 35 * KB);
            assertTrue(bytesRead != -1);
            assertEquals(35 * KB, chunkData.length);

            // Create ChunkStoreRequest with 35KB chunk data
            String testAbsolutePath = "/test_directory/test_file2.data";
            Integer testSequence = 3;
            ChunkStoreRequest chunkStoreRequest = new ChunkStoreRequest("localhost", "192.168.0.1", 35486,
                    testReplicationChunkServers, testAbsolutePath, testSequence, chunkData);

            // Create ChunkServerProcessor and process request
            ChunkServer chunkServer = new ChunkServer("localhost", 9000);
            ChunkServerProcessor csp = new ChunkServerProcessor(null, chunkServer);
            csp.processChunkStoreRequest(chunkStoreRequest);

            // Assert file has been created
            String chunkFilename = String.format("%s%s_chunk%d", getTestResourcesPath(), testAbsolutePath, testSequence);
            File testChunkFile = new File(chunkFilename);
            assertTrue(testChunkFile.exists());

            // Load chunk and assert version is 1, chunk integrity is valid,
            ChunkFilename filename = new ChunkFilename(chunkFilename, Chunk.getChunkDir());
            Chunk loadedChunk = Chunk.load(filename);
            assertEquals(1, loadedChunk.metadata.getVersion());
            assertTrue(loadedChunk.integrity.isChunkValid(loadedChunk.data));
            assertEquals(35 * KB, loadedChunk.metadata.getSizeBytes());
            assertEquals(3, loadedChunk.metadata.getSequence());

            // Send another request, exactly the same (it should update it instead of first-time saving)
            csp.processChunkStoreRequest(chunkStoreRequest);

            // Reload chunk and assert version is 2
            loadedChunk = Chunk.load(filename);
            assertEquals(2, loadedChunk.metadata.getVersion());

            // Clean up
            assertTrue(testChunkFile.delete());
        } catch (IOException e) {
            fail("Caught IOException!");
        }
        Chunk.setChunkDir(previousChunkDir);
    }

}
