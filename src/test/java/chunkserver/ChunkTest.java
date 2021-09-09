package chunkserver;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
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
    public void testLoadChunk() {
        String previousChunkDir = Chunk.getChunkDir();
        Chunk.setChunkDir(getTestResourcesPath());
        try {
            Chunk chunk = Chunk.load("/test_35kb_chunk.data", 3);
            System.out.println(chunk);

        } catch (IOException e) {
            System.err.println(e.getMessage());
            fail("Caught IOException!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getCause());
            fail("Caught Exception!");
        }
        Chunk.setChunkDir(previousChunkDir);
    }
}
