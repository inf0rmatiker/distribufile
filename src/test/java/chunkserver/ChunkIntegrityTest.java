package chunkserver;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static util.Constants.CHUNK_SIZE;
import static util.Constants.KB;

public class ChunkIntegrityTest {

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
    public void testCalculateSHA1TestString() {
        String testString = "test string";
        String expected = "661295c9cbf9d6b2f6428414504a8deed3020641";
        byte[] testSliceBytes = testString.getBytes();
        String actual = ChunkIntegrity.calculateSHA1(testSliceBytes);
        assertEquals(expected, actual);
    }

    @Test
    public void testCalculateSHA1Empty() {
        String testString = "";
        String expected = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
        byte[] testSliceBytes = testString.getBytes();
        String actual = ChunkIntegrity.calculateSHA1(testSliceBytes);
        assertEquals(expected, actual);
    }

    @Test
    public void testStringToHexToString() {
        String expected = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
        try {
            byte[] hexArray = ChunkIntegrity.hexStringToBytes(expected);
            String actual = ChunkIntegrity.bytesToHexString(hexArray);
            assertEquals(expected, actual);
        } catch (DecoderException e) {
            fail("Invalid hex format");
        }
    }

    @Test
    public void testCalculateSliceChecksums() {
        String absoluteFilePath = getTestResourceAbsolutePath(testFiles[1]);
        List<String> expecteds = new ArrayList<>(Arrays.asList(
                "ffabe7dd8cf71b40367392950340ca9c4e7d707c",
                "02217ffa9bf6089d892c90fcc8cd2c79eb93fdbf",
                "9d47732fa8be3ae8c43c3d71fd3223d1503d9bf8",
                "71ecc2b28d2a6b7195f45982583490b1ac12c245",
                "2403616b097d5fa3664e48ce0a000e991e795092"
        ));

        try {
            FileInputStream fileInputStream = new FileInputStream(absoluteFilePath);
            BufferedInputStream reader = new BufferedInputStream(fileInputStream, CHUNK_SIZE);

            byte[] chunk = new byte[35 * KB];
            int bytesRead = reader.read(chunk, 0, 35 * KB);

            assertTrue(bytesRead != -1);
            assertEquals(35 * KB, chunk.length);

            List<String> actuals = ChunkIntegrity.calculateSliceChecksums(chunk);
            for (int i = 0; i < expecteds.size(); i++) {
                assertEquals(expecteds.get(i), actuals.get(i));
            }

            reader.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testIsValidChunkUnchangedData() {
        String absoluteFilePath = getTestResourceAbsolutePath(testFiles[1]);

        try {
            FileInputStream fileInputStream = new FileInputStream(absoluteFilePath);
            BufferedInputStream reader = new BufferedInputStream(fileInputStream, CHUNK_SIZE);

            byte[] chunk = new byte[35 * KB];
            int bytesRead = reader.read(chunk, 0, 35 * KB);

            assertTrue(bytesRead != -1);
            assertEquals(35 * KB, chunk.length);
            List<String> expecteds = new ArrayList<>(Arrays.asList(
                    "ffabe7dd8cf71b40367392950340ca9c4e7d707c",
                    "02217ffa9bf6089d892c90fcc8cd2c79eb93fdbf",
                    "9d47732fa8be3ae8c43c3d71fd3223d1503d9bf8",
                    "71ecc2b28d2a6b7195f45982583490b1ac12c245",
                    "2403616b097d5fa3664e48ce0a000e991e795092"
            ));
            ChunkIntegrity testChunkIntegrity = new ChunkIntegrity(expecteds);
            assertTrue(testChunkIntegrity.isChunkValid(chunk));

            reader.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testIsValidChunkChangedData() {
        String absoluteFilePath = getTestResourceAbsolutePath(testFiles[1]);

        try {
            FileInputStream fileInputStream = new FileInputStream(absoluteFilePath);
            BufferedInputStream reader = new BufferedInputStream(fileInputStream, CHUNK_SIZE);

            byte[] chunk = new byte[35 * KB];
            int bytesRead = reader.read(chunk, 0, 35 * KB);

            assertTrue(bytesRead != -1);
            assertEquals(35 * KB, chunk.length);
            List<String> expecteds = new ArrayList<>(Arrays.asList(
                    "ffabe7dd8cf71b40367392950340ca9c4e7d707c",
                    "02217ffa9bf6089d892c90fcc8cd2c79eb93fdbf",
                    "9d47732fa8be3ae8c43c3d71fd3223d1503d9bf8",
                    "71ecc2b28d2a6b7195f45982583490b1ac12c245",
                    "2403616b097d5fa3664e48ce0a000e991e795092"
            ));

            // Modify chunk bytes at an arbitrary location and assert the checksums fail
            chunk[0] = 0x00; chunk[1] = 0x00; chunk[2] = 0x00;
            ChunkIntegrity testChunkIntegrity = new ChunkIntegrity(expecteds);
            assertFalse(testChunkIntegrity.isChunkValid(chunk));

            reader.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }

    @Test
    public void testIsValidChunkChangedReadChecksums() {
        String absoluteFilePath = getTestResourceAbsolutePath(testFiles[1]);

        try {
            FileInputStream fileInputStream = new FileInputStream(absoluteFilePath);
            BufferedInputStream reader = new BufferedInputStream(fileInputStream, CHUNK_SIZE);

            byte[] chunk = new byte[35 * KB];
            int bytesRead = reader.read(chunk, 0, 35 * KB);

            assertTrue(bytesRead != -1);
            assertEquals(35 * KB, chunk.length);

            // Modify checksums that are "read" and assert it causes failure
            List<String> expecteds = new ArrayList<>(Arrays.asList(
                    "ffabe7dd8cf71b40367392950340ca9c4e7d707c",
                    "02217ffa9bf6089d892c90fcc8cd2c79eb93fdbf",
                    "cc47732fa8be3ae8c43c3d71fd3223d1503d9bf8", // should start with "9d..." instead of "cc..."
                    "71ecc2b28d2a6b7195f45982583490b1ac12c245",
                    "2403616b097d5fa3664e48ce0a000e991e795092"
            ));
            ChunkIntegrity testChunkIntegrity = new ChunkIntegrity(expecteds);
            assertFalse(testChunkIntegrity.isChunkValid(chunk));

            reader.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }
    }
}
