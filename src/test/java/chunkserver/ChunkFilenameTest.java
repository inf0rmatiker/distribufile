package chunkserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkFilenameTest {

    @Test
    public void testAssignmentExample() {
        String testChunkDir = "/tmp";
        String testAbsolutePath = "/user/bob/experiment/SimFile.data";
        Integer testChunkSequence = 3;

        ChunkFilename chunkFilename = new ChunkFilename(testAbsolutePath, testChunkDir, testChunkSequence);
        assertEquals("/tmp", chunkFilename.getChunkDir());
        assertEquals("/user/bob/experiment/", chunkFilename.getBase());
        assertEquals("SimFile.data", chunkFilename.getFilename());
        assertEquals("_chunk3", chunkFilename.getChunkSuffix());
        assertEquals("/tmp/user/bob/experiment/SimFile.data_chunk3", chunkFilename.getChunkFilename());
        assertEquals(testAbsolutePath, chunkFilename.getClientAbsolutePath());
    }

    @Test
    public void testConstructFromChunkFilename() {
        String testChunkFilename = "/tmp/path/to/my/file.data_chunk5";
        ChunkFilename chunkFilename = new ChunkFilename(testChunkFilename, "/tmp");

        assertEquals("/tmp", chunkFilename.getChunkDir());
        assertEquals("/path/to/my/", chunkFilename.getBase());
        assertEquals("file.data", chunkFilename.getFilename());
        assertEquals("_chunk5", chunkFilename.getChunkSuffix());
        assertEquals("/path/to/my/file.data", chunkFilename.getClientAbsolutePath());
        assertEquals("/tmp/path/to/my/", chunkFilename.getChunkBase());
    }
}
