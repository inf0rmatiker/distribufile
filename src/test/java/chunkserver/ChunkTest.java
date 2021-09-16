package chunkserver;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static util.Constants.CHUNK_SIZE;
import static util.Constants.KB;

public class ChunkTest {

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
    public void testLoadSavedChunk() {
        Chunk.setChunkDir(getTestResourcesPath());
        String testFile = getTestResourceAbsolutePath(testFiles[1]);
        String testFileRelativePath = "/" + testFiles[1];
        try {

            // Read raw chunk data from 35KB file
            FileInputStream fileInputStream = new FileInputStream(testFile);
            BufferedInputStream reader = new BufferedInputStream(fileInputStream, CHUNK_SIZE);
            byte[] chunkData = new byte[35 * KB];
            int bytesRead = reader.read(chunkData, 0, 35 * KB);
            assertTrue(bytesRead != -1);
            assertEquals(35 * KB, chunkData.length);

            // Clean up input streams
            reader.close();
            fileInputStream.close();

            // Build an in-memory chunk from above data
            int testSequence = 0;
            ChunkMetadata metadata = new ChunkMetadata(testFileRelativePath, testSequence, chunkData.length);
            List<String> sliceChecksums = new ArrayList<>(Arrays.asList(
                    "ffabe7dd8cf71b40367392950340ca9c4e7d707c",
                    "02217ffa9bf6089d892c90fcc8cd2c79eb93fdbf",
                    "9d47732fa8be3ae8c43c3d71fd3223d1503d9bf8",
                    "71ecc2b28d2a6b7195f45982583490b1ac12c245",
                    "2403616b097d5fa3664e48ce0a000e991e795092"
            ));
            ChunkIntegrity integrity = new ChunkIntegrity(sliceChecksums);
            Chunk testChunk = new Chunk(metadata, integrity, chunkData);

            // Save testChunk to disk and reload, asserting equivalence
            ChunkFilename filename = new ChunkFilename(testFileRelativePath, getTestResourcesPath(), testSequence);
            Chunk.save(testChunk, filename);
            Chunk reloadedChunk = Chunk.load(filename);
            assertEquals(testChunk, reloadedChunk);

            // Clean up the mess
            File f = new File(String.format("%s%s_chunk%d", getTestResourcesPath(), testFileRelativePath, testSequence));
            if(f.exists() && !f.isDirectory()) {
                assertTrue(f.delete());
            } else {
                fail("Expected a test file to be created, did not find one!");
            }
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            fail("Caught IOException!");
        } finally {
            // Reset env back to default
            Chunk.setChunkDir("/tmp");
        }
    }

    @Test
    public void testUpdateChunk() {
        Chunk.setChunkDir(getTestResourcesPath());
        String testFile = getTestResourceAbsolutePath(testFiles[1]);
        String testFileRelativePath = "/" + testFiles[1];
        try {

            // Read raw chunk data from 35KB file
            FileInputStream fileInputStream = new FileInputStream(testFile);
            BufferedInputStream reader = new BufferedInputStream(fileInputStream, CHUNK_SIZE);
            byte[] chunkData = new byte[35 * KB];
            int bytesRead = reader.read(chunkData, 0, 35 * KB);
            assertTrue(bytesRead != -1);
            assertEquals(35 * KB, chunkData.length);

            // Clean up input streams
            reader.close();
            fileInputStream.close();

            // Build an in-memory chunk from above data
            int testSequence = 0;
            ChunkMetadata metadata = new ChunkMetadata(testFileRelativePath, testSequence, chunkData.length);
            List<String> sliceChecksums = new ArrayList<>(Arrays.asList(
                    "ffabe7dd8cf71b40367392950340ca9c4e7d707c",
                    "02217ffa9bf6089d892c90fcc8cd2c79eb93fdbf",
                    "9d47732fa8be3ae8c43c3d71fd3223d1503d9bf8",
                    "71ecc2b28d2a6b7195f45982583490b1ac12c245",
                    "2403616b097d5fa3664e48ce0a000e991e795092"
            ));
            ChunkIntegrity integrity = new ChunkIntegrity(sliceChecksums);
            Chunk testChunk = new Chunk(metadata, integrity, chunkData);

            // Save testChunk to disk
            ChunkFilename filename = new ChunkFilename(testFileRelativePath, getTestResourcesPath(), testSequence);
            Chunk.save(testChunk, filename);

            // "Update" chunk with same chunk, version should increment
            Chunk.update(testChunk, filename);

            // Reload chunk and assert version has incremented to 2
            Chunk reloadedChunk = Chunk.load(filename);
            assertEquals(2, reloadedChunk.metadata.getVersion());

            // Clean up the mess
            File f = new File(String.format("%s%s_chunk%d", getTestResourcesPath(), testFileRelativePath, testSequence));
            if(f.exists() && !f.isDirectory()) {
                assertTrue(f.delete());
            } else {
                fail("Expected a test file to be created, did not find one!");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            fail("Caught IOException!");
        }
    }

    @Test
    public void testMakeParentDirsIfNotExist() {
        String absolutePathToTestResources = getTestResourcesPath();
        Chunk.setChunkDir(absolutePathToTestResources);
        ChunkFilename filename = new ChunkFilename("/path/to/my/file.data", absolutePathToTestResources, 3);

        try {
            Chunk.makeParentDirsIfNotExist(filename);

            File parentDirectory = new File(absolutePathToTestResources + "/path/to/my/");
            assertTrue(parentDirectory.exists());
            assertTrue(parentDirectory.isDirectory());

            // Clean up
            FileUtils.deleteDirectory(new File(absolutePathToTestResources + "/path"));
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
