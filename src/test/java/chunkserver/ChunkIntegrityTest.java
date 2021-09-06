package chunkserver;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ChunkIntegrityTest {

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
}
