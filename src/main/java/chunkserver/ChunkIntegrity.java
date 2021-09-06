package chunkserver;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChunkIntegrity {

    Logger log = LoggerFactory.getLogger(ChunkIntegrity.class);

    // A list of SHA-1 checksums, one for each slice of the chunk
    public List<String> sliceChecksums;

    public ChunkIntegrity() {
        this.sliceChecksums = new ArrayList<>();
    }

    public List<String> getSliceChecksums() {
        return sliceChecksums;
    }

    /**
     * Calculates the SHA-1 hash text of a slice byte array.
     * Code structure taken from https://www.geeksforgeeks.org/sha-1-hash-in-java/ with modifications
     * made to string building efficiency.
     * @param slice Raw bytes of a slice, part of a chunk.
     * @return Returns a String of length 40, representing the 20-byte (160-bit) hash code in hexadecimal.
     *  Note: remember that each byte is represented with 2 hex characters, we get 40 characters for 20 bytes.
     */
    public static String calculateSHA1(byte[] slice) {
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest messageDigestInstance = MessageDigest.getInstance("SHA-1");

            // digest() method is called to calculate message digest of the slice
            byte[] messageDigest = messageDigestInstance.digest(slice);

            // Convert byte array into signum representation
            BigInteger bigNumber = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            StringBuilder hashText = new StringBuilder(bigNumber.toString(16));

            // Add preceding 0s to make it 32 bit
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }

            // return the HashText
            return hashText.toString();

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Unable to find algorithm!");
            return "";
        }
    }

    /**
     * Converts a string of hexadecimal characters to the equivalent hex byte array, shrinking size by half.
     * @param hexCharacters String human-readable hex characters
     * @return Raw hex bytes
     * @throws DecoderException If the input string has non-hex characters
     */
    public static byte[] hexStringToBytes(String hexCharacters) throws DecoderException {
        return Hex.decodeHex(hexCharacters.toCharArray());
    }

    /**
     * Converts a raw hex byte array to a UTF-8 encoded String.
     * @param hexArray Raw bytes
     * @return String of hex in UTF-8 encoding.
     */
    public static String bytesToHexString(byte[] hexArray) {
        return Hex.encodeHexString(hexArray);
    }

}
