package controller;

import org.junit.jupiter.api.Test;

import chunkserver.ChunkMetadata;


import static org.junit.jupiter.api.Assertions.*;
import static util.Constants.MB;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class FileMetadataTest {

    @Test
    public void testFileArgsConstructor() {
        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data");
        assertEquals("/s/tmp/test.data", fileMetadata.absolutePath);
        assertEquals(0, fileMetadata.chunkServerHostnames.size());
    }

    @Test
    public void testFullArgsConstructor() {
        Vector<Set<String>> chunksServerHostnames = new Vector<>();
        chunksServerHostnames.add(new HashSet<>() {
            {
                add("shark");
            }
        });

        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data", chunksServerHostnames);
        assertEquals("/s/tmp/test.data", fileMetadata.absolutePath);
        assertEquals(1, fileMetadata.chunkServerHostnames.size());
        assertEquals(1, fileMetadata.chunkServerHostnames.get(0).size());
        assertTrue(fileMetadata.chunkServerHostnames.get(0).contains("shark"));
    }

    @Test
    public void testEquals() {
        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data");
        FileMetadata fileMetadata2 = new FileMetadata("/s/tmp/test.data");
        assertEquals(fileMetadata, fileMetadata2); // two FileMetadata Objects
    }

    @Test
    public void testFillChunkGaps() {
        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data");
        assertNotNull(fileMetadata.chunkServerHostnames);
        assertTrue(fileMetadata.chunkServerHostnames.isEmpty());

        fileMetadata.fillChunkGaps(5);
        assertEquals(6, fileMetadata.chunkServerHostnames.size());
    }

    @Test
    public void testPutBeyondEnd() {
        Vector<Set<String>> testHostnames = new Vector<>();
        testHostnames.add(new HashSet<>() { // 0
            {
                add("shark");
            }
        });
        testHostnames.add(new HashSet<>()); // 1
        testHostnames.add(new HashSet<>()); // 2
        testHostnames.add(new HashSet<>()); // 3
        testHostnames.add(new HashSet<>()); // 4
        testHostnames.add(new HashSet<>() { // 5
            {
                add("tuna");
                add("swordfish");
            }
        });

        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data", testHostnames);

        // Add beyond the end
        fileMetadata.put("catfish", 7);
        assertEquals(8, fileMetadata.chunkServerHostnames.size());
        assertTrue(fileMetadata.chunkServerHostnames.get(7).contains("catfish"));
    }

    @Test
    public void testPutAlreadyExists() {
        Vector<Set<String>> testHostnames = new Vector<>();
        testHostnames.add(new HashSet<>() { // 0
            {
                add("shark");
            }
        });
        testHostnames.add(new HashSet<>()); // 1
        testHostnames.add(new HashSet<>()); // 2
        testHostnames.add(new HashSet<>()); // 3
        testHostnames.add(new HashSet<>()); // 4
        testHostnames.add(new HashSet<>() { // 5
            {
                add("tuna");
                add("swordfish");
            }
        });

        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data", testHostnames);

        fileMetadata.put("shark", 0);
        assertEquals(6, fileMetadata.chunkServerHostnames.size());
        assertTrue(fileMetadata.chunkServerHostnames.get(0).contains("shark"));
    }

    @Test
    public void testPutInMiddle() {
        Vector<Set<String>> testHostnames = new Vector<>();
        testHostnames.add(new HashSet<>() { // 0
            {
                add("shark");
            }
        });
        testHostnames.add(new HashSet<>()); // 1
        testHostnames.add(new HashSet<>()); // 2
        testHostnames.add(new HashSet<>()); // 3
        testHostnames.add(new HashSet<>()); // 4
        testHostnames.add(new HashSet<>() { // 5
            {
                add("tuna");
                add("swordfish");
            }
        });

        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data", testHostnames);

        fileMetadata.put("shark", 3);
        assertEquals(6, fileMetadata.chunkServerHostnames.size());
        assertTrue(fileMetadata.chunkServerHostnames.get(3).contains("shark"));
    }

    @Test
    public void testPutSizeEqualsSequence() {
        Vector<Set<String>> testHostnames = new Vector<>();
        testHostnames.add(new HashSet<>() { // 0
            {
                add("shark");
            }
        });
        testHostnames.add(new HashSet<>()); // 1
        testHostnames.add(new HashSet<>()); // 2
        testHostnames.add(new HashSet<>()); // 3
        testHostnames.add(new HashSet<>()); // 4
        testHostnames.add(new HashSet<>() { // 5
            {
                add("tuna");
                add("swordfish");
            }
        });

        FileMetadata fileMetadata = new FileMetadata("/s/tmp/test.data", testHostnames);

        int previousSize = fileMetadata.chunkServerHostnames.size();
        assertEquals(6, previousSize);
        fileMetadata.put("shark", previousSize);
        int newSize = fileMetadata.chunkServerHostnames.size();
        assertEquals(previousSize + 1, newSize);
    }

}
