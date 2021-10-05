package chunkserver;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Holds integrity information (SHA-1 hash checksums) for each of the slices of a chunk.
 * Provides utility functions for calculating and manipulating above information.
 */
public class ChunkIntegrity {

    public static Logger log = LoggerFactory.getLogger(ChunkIntegrity.class);

    // A list of SHA-1 checksums, one for each slice of the chunk
    public List<String> sliceChecksums;

    /**
     * Use this constructor when reading pre-computed slice checksums from a stored chunk file.
     * @param sliceChecksums Read slice checksums from the file's integrity information
     */
    public ChunkIntegrity(List<String> sliceChecksums) {
        this.sliceChecksums = sliceChecksums;
    }

    /**
     * Use this constructor for calculating integrity information of a chunk for the first time.
     * @param chunk Raw bytes of the chunk
     */
    public ChunkIntegrity(byte[] chunk) {
        this.sliceChecksums = calculateSliceChecksums(chunk);
    }


    public List<String> getSliceChecksums() {
        return sliceChecksums;
    }

    /**
     * Validates the chunk data by comparing the checksums we read in with the checksums calculated on the actual
     * chunk data. If they don't match then one or the other has been altered.
     * @param chunk The actual chunk data as a byte array.
     * @return True if all the checksums match, false otherwise.
     */
    public boolean isChunkValid(byte[] chunk) {
        List<String> actualChecksums = calculateSliceChecksums(chunk);
        if (actualChecksums.size() != this.sliceChecksums.size()) {
            log.error("Expected {} checksums, only calculated {}", this.sliceChecksums.size(), actualChecksums.size());
            return false;
        }
        for (int i = 0; i < actualChecksums.size(); i++) {
            String actualChecksum = actualChecksums.get(i);
            String expectedChecksum = this.sliceChecksums.get(i);
            if (!actualChecksum.equals(expectedChecksum)) {
                log.error("Expected checksums[{}] to be {}, got {} instead", i, expectedChecksum, actualChecksum);
                return false;
            }
        }
        log.info("All checksums match; chunk is valid");
        return true;
    }

    /**
     * Calculates the SHA-1 checksums for each of the slices in the chunk, and adds them to the in-memory
     * sliceChecksums List.
     * @param chunk The raw bytes of the entire chunk
     * @return A SHA-1 hash text checksum for each of the slices within the chunk
     */
    public static List<String> calculateSliceChecksums(byte[] chunk) {
        List<String> checksums = new ArrayList<>();

        try {
            // Init input stream from chunk
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(chunk);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

            byte[] slice = new byte[Constants.SLICE_SIZE];
            int bytesRead = dataInputStream.read(slice);
            int sliceIndex = 0;
            while (bytesRead != -1) {

                // Resize slice if necessary (for last slice in chunk)
                if (bytesRead < Constants.SLICE_SIZE) {
                    byte[] resizedSlice = new byte[bytesRead];
                    System.arraycopy(slice, 0, resizedSlice, 0, resizedSlice.length);
                    slice = resizedSlice;
                }

                checksums.add(calculateSHA1(slice));
                log.info("Slice {} of size {} bytes: hash={}", sliceIndex, bytesRead, checksums.get(checksums.size()-1));
                bytesRead = dataInputStream.read(slice); // read the next slice
                sliceIndex++;
            }
            log.info("Finished reading final slice of chunk");

            dataInputStream.close();
            byteInputStream.close();
        } catch (IOException e) {
            log.error("ChunkIntegrity::calculateSliceChecksums(): Caught IOException while attempting to close() input streams!");
        }

        return checksums;
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

            // Add preceding 0s to make it 20 bytes (40 characters)
            while (hashText.length() < 40) {
                hashText.insert(0, "0");
            }

            // return the HashText
            return hashText.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to find SHA-1 MessageDigest algorithm!");
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Chunk Integrity:\n");
        sb.append(String.format("\tSlices: %d\n", this.sliceChecksums.size()));
        for (String sliceChecksum: this.sliceChecksums) {
            sb.append(String.format("\t\tChecksum: %s\n", sliceChecksum));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkIntegrity)) return false;
        ChunkIntegrity ciOther = (ChunkIntegrity) other;
        return (this.sliceChecksums.equals(ciOther.getSliceChecksums()));
    }

}
