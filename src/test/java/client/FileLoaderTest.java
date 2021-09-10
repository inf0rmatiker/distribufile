package client;

import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static util.Constants.KB;

public class FileLoaderTest {

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

    @Test
    public void testConstructorFileNotExists() {
        assertThrows(FileNotFoundException.class, () -> new FileLoader("non_existent_file.data"));
    }

    @Test
    public void testConstructorExistentFile() {
        assertDoesNotThrow(() -> {
            FileLoader fileLoader = new FileLoader(getTestResourceAbsolutePath(testFiles[0]));
            fileLoader.close();
        });
    }

    @Test
    public void testReadChunkEmptyFile() {
        try {
            FileLoader fileLoader = new FileLoader(getTestResourceAbsolutePath(testFiles[0]));
            byte[] actual = fileLoader.readChunk();
            assertNull(actual);
            fileLoader.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testReadChunk35KBfile() {
        try {
            FileLoader fileLoader = new FileLoader(getTestResourceAbsolutePath(testFiles[1]));
            int[] expecteds = new int[]{35 * KB};

            for (int expectedLength: expecteds) {
                byte[] actual = fileLoader.readChunk();
                assertNotNull(actual);
                assertEquals(expectedLength, actual.length);
            }
            assertNull(fileLoader.readChunk()); // make sure trying to read past expected produces null
            fileLoader.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testReadChunk64KBfile() {
        try {
            FileLoader fileLoader = new FileLoader(getTestResourceAbsolutePath(testFiles[2]));
            int[] expecteds = new int[]{64 * KB};

            for (int expectedLength: expecteds) {
                byte[] actual = fileLoader.readChunk();
                assertNotNull(actual);
                assertEquals(expectedLength, actual.length);
            }
            assertNull(fileLoader.readChunk()); // make sure trying to read past expected produces null
            fileLoader.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testReadChunk100KBfile() {
        try {
            FileLoader fileLoader = new FileLoader(getTestResourceAbsolutePath(testFiles[3]));
            int[] expecteds = new int[]{64 * KB, 36 * KB};

            for (int expectedLength: expecteds) {
                byte[] actual = fileLoader.readChunk();
                assertNotNull(actual);
                assertEquals(expectedLength, actual.length);
            }
            assertNull(fileLoader.readChunk()); // make sure trying to read past expected produces null
            fileLoader.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testReadChunk680KBfile() {
        try {
            FileLoader fileLoader = new FileLoader(getTestResourceAbsolutePath(testFiles[4]));

            // 10x 64KB chunks, 1x 40KB chunk
            int[] expecteds = new int[]{
                    64 * KB,
                    64 * KB,
                    64 * KB,
                    64 * KB,
                    64 * KB,
                    64 * KB,
                    64 * KB,
                    64 * KB,
                    64 * KB,
                    64 * KB,
                    40 * KB
            };

            for (int expectedLength: expecteds) {
                byte[] actual = fileLoader.readChunk();
                assertNotNull(actual);
                assertEquals(expectedLength, actual.length);
            }
            assertNull(fileLoader.readChunk()); // make sure trying to read past expected produces null
            fileLoader.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
