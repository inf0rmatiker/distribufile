package chunkserver;

import messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static util.Constants.KB;

public class Chunk {

    public static Logger log = LoggerFactory.getLogger(Chunk.class);
    private static String CHUNK_DIR;

    public ChunkMetadata metadata;

    public ChunkIntegrity integrity;

    public byte[] data;

    public Chunk(ChunkMetadata metadata, ChunkIntegrity integrity, byte[] data) {
        this.metadata = metadata;
        this.integrity = integrity;
        this.data = data;
    }

    /**
     * Loads a chunk, along with its metadata and integrity information, from disk.
     * @param absolutePath The absolute path of the chunk's parent file.
     * @param sequence The sequence number of the chunk within the parent file.
     * @return A fully-populated Chunk in-memory, along with its metadata and integrity information
     * @throws IOException If unable to read file.
     */
    public static Chunk load(String absolutePath, Integer sequence) throws IOException {

        /*
          If the file name is /user/bob/experiment/SimFile.data, chunk 2 of this file will be stored by a chunk server
          as /tmp/user/bob/experiment/SimFile.data_chunk2
         */
        String chunkPath = String.format("%s%s_chunk%d", getChunkDir(), absolutePath, sequence);
        log.info("Loading chunk from file \"{}\"", chunkPath);
        Path path = Paths.get(chunkPath);
        int storedChunkSize = (int) Files.size(path); // size of chunk in bytes including metadata/integrity information
        log.info("Stored chunk size: {}", storedChunkSize);

        // Read all stored chunk bytes into memory (shouldn't be too expensive, since only a chunk)
        FileInputStream fileInputStream = new FileInputStream(chunkPath);
        BufferedInputStream reader = new BufferedInputStream(fileInputStream, 8 * KB);
        byte[] storedChunk = reader.readNBytes(storedChunkSize);
        reader.close();
        fileInputStream.close();

        log.info("Stored chunk byte array size: {}", storedChunk.length);

        // Initialize buffered streams for reading bytes into meaningful data types
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(storedChunk);
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteInputStream));

        // Read chunk metadata
        ChunkMetadata metadata = Message.readChunkMetadata(dataInputStream);

        // Read chunk integrity data
        List<String> sliceChecksums = Message.readStringList(dataInputStream);
        ChunkIntegrity integrity = new ChunkIntegrity(sliceChecksums);

        // Read actual chunk data
        byte[] chunkData = dataInputStream.readNBytes(metadata.getSizeBytes());

        // Construct Chunk
        Chunk chunk = new Chunk(metadata, integrity, chunkData);

        // Clean up buffered streams and return Chunk
        dataInputStream.close();
        byteInputStream.close();
        return chunk;
    }

    /**
     * Saves a chunk to disk, with its metadata and integrity information.
     * @param chunk The chunk we are saving to disk.
     * @throws IOException If unable to write to file.
     */
    public static void save(Chunk chunk) throws IOException {
        /*
          If the file name is /user/bob/experiment/SimFile.data, chunk 2 of this file will be stored by a chunk server
          as /tmp/user/bob/experiment/SimFile.data_chunk2
         */
        String chunkPath = String.format("%s%s_chunk%d", getChunkDir(), chunk.metadata.getAbsoluteFilePath(),
                chunk.metadata.getSequence());

        FileOutputStream fileOutputStream = new FileOutputStream(chunkPath);
        DataOutputStream dataOutStream = new DataOutputStream(fileOutputStream);

        Message.writeChunkMetadata(dataOutStream, chunk.metadata);
        Message.writeStringList(dataOutStream, chunk.integrity.getSliceChecksums());
        dataOutStream.write(chunk.data, 0, chunk.data.length);

        dataOutStream.close();
        fileOutputStream.close();
    }

    /**
     * Gets the directory on the Chunk Server's filesystem where chunks are stored.
     * @return Directory as a String, or default value if none set.
     */
    public static String getChunkDir() {
        if (CHUNK_DIR == null || CHUNK_DIR.trim().isEmpty()) {
            log.warn("chunk variable not set. Using /tmp as default");
            return "/tmp";
        }
        return CHUNK_DIR;
    }

    /**
     * Sets the directory on the Chunk Server's filesystem where chunks are stored.
     * @param dir Directory as a String
     */
    public static void setChunkDir(String dir) {
        CHUNK_DIR = dir;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.metadata.toString());
        sb.append(this.integrity.toString());
        sb.append("Chunk:\n");
        int numberFullSizedSlices = this.metadata.getSizeBytes() / Constants.SLICE_SIZE;
        for (int i = 0; i < numberFullSizedSlices; i++) {
            sb.append(String.format("\t<-- Slice[%d], %d bytes -->\n", i, Constants.SLICE_SIZE));
        }

        int remainingBytes = this.metadata.getSizeBytes() % Constants.SLICE_SIZE;
        if (remainingBytes > 0) {
            sb.append(String.format("\t<-- Slice[%d], %d bytes -->\n", numberFullSizedSlices, remainingBytes));
        }
        return sb.toString();
    }

}
