package chunkserver;

import messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static util.Constants.KB;

public class Chunk {

    public static Logger log = LoggerFactory.getLogger(Chunk.class);
    private static String CHUNK_DIR; // Not visible for external use on purpose

    // Chunk metadata
    public ChunkMetadata metadata;

    // Chunk integrity checksums for each of the slices
    public ChunkIntegrity integrity;

    // Raw bytes of the chunk itself
    public byte[] data;

    public Chunk(ChunkMetadata metadata, ChunkIntegrity integrity, byte[] data) {
        this.metadata = metadata;
        this.integrity = integrity;
        this.data = data;
    }

    /**
     * @return The boolean validity of the chunk data
     */
    public boolean isValid() {
        return this.integrity.isChunkValid(this.data);
    }

    /**
     * Loads a chunk, along with its metadata and integrity information, from disk.
     * @param filename ChunkFilename of the chunk file we are trying to load
     * @return A fully-populated Chunk in-memory, along with its metadata and integrity information
     * @throws IOException If unable to read file.
     */
    public static Chunk load(ChunkFilename filename) throws IOException {
        String chunkPath = filename.getChunkFilename();

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

        // Read Chunk
        Chunk chunk = Message.readChunk(dataInputStream);

        // Clean up buffered streams and return Chunk
        dataInputStream.close();
        byteInputStream.close();
        return chunk;
    }

    /**
     * Saves a chunk to disk, with its metadata and integrity information.
     * @param chunk The chunk we are saving to disk.
     * @param filename ChunkFilename of the chunk file we are saving.
     * @throws IOException If unable to write to file.
     */
    public static void save(Chunk chunk, ChunkFilename filename) throws IOException {
        log.info("Writing chunk to {}", filename);
        try {
            makeParentDirsIfNotExist(filename);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // Initialize output streams for writing to file
        FileOutputStream fileOutputStream = new FileOutputStream(filename.getChunkFilename());
        DataOutputStream dataOutStream = new DataOutputStream(fileOutputStream);

        // Write chunk metadata, integrity information, and raw data to disk
        Message.writeChunk(dataOutStream, chunk);

        // Clean up output streams
        dataOutStream.flush();
        dataOutStream.close();
        fileOutputStream.close();
        log.info("Successfully saved chunk {}", filename);
    }

    /**
     * Updates a chunk file, overwriting its data/integrity information/metadata, and increments the version.
     * @param chunk Chunk containing new data, metadata, and integrity information
     * @param filename ChunkFilename components telling us where to read/write to
     * @throws IOException If file not found or could not read/write
     */
    public static void update(Chunk chunk, ChunkFilename filename) throws IOException {
        String chunkPath = filename.getChunkFilename();

        // Read chunk version
        FileInputStream fileInputStream = new FileInputStream(chunkPath);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        int chunkVersion = dataInputStream.readInt();
        dataInputStream.close();
        fileInputStream.close();
        chunk.metadata.version = chunkVersion + 1; // set metadata version to read version + 1
        log.info("Updating chunk version from {} to {}", chunkVersion, chunk.metadata.version);

        // Overwrite the old chunk file with the new chunk, metadata, and integrity info
        save(chunk, filename);
        log.info("Successfully updated chunk {}", filename);
    }

    /**
     * Reads just the metadata associated with a chunk file
     * @param filename ChunkFilename of the chunk
     * @return ChunkMetadata - in-memory metadata of the chunk
     * @throws IOException If unable to read from file
     */
    public static ChunkMetadata readChunkMetadata(ChunkFilename filename) throws IOException {
        String chunkPath = filename.getChunkFilename();

        log.info("Loading chunk metadata from file \"{}\"", chunkPath);
        FileInputStream fileInputStream = new FileInputStream(chunkPath);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        ChunkMetadata metadata = Message.readChunkMetadata(dataInputStream);
        dataInputStream.close();
        fileInputStream.close();

        return metadata;
    }

    /**
     * Creates all the parent directories for a chunk file, if they don't already exist or only partially exist
     * @param filename The ChunkFilename object containing all the parts of the chunk's filename
     * @throws IOException If:
     * - unable to read/write to the directory specified
     * - part of the path exists as a file, not a directory
     */
    public static void makeParentDirsIfNotExist(ChunkFilename filename) throws IOException {
        File chunkFileDirectory = new File(filename.getChunkBase());
        if (!chunkFileDirectory.exists()) {
            if (!chunkFileDirectory.mkdirs()) {
                throw new IOException("Unable to create directory " + filename.getChunkBase());
            }
        } else if (chunkFileDirectory.isFile()) {
            throw new IOException(filename.getChunkBase() + " is a file");
        }
    }

    /**
     * Checks existence of chunk file
     * @param filename ChunkFilename object
     * @return True if chunk file exists, false otherwise
     * @throws IOException If unable to read
     */
    public static Boolean alreadyExists(ChunkFilename filename) throws IOException {
        File chunkFile = new File(filename.getChunkFilename());
        return chunkFile.exists();
    }

    /**
     * Gets the directory on the Chunk Server's filesystem where chunks are stored.
     * @return Directory as a String, or default value if none set.
     */
    public static String getChunkDir() {
        if (CHUNK_DIR == null || CHUNK_DIR.trim().isEmpty()) {
            log.warn("chunk variable not set. Using /tmp as default");
            CHUNK_DIR = "/tmp";
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

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Chunk)) return false;
        Chunk cOther = (Chunk) other;
        return (this.metadata.equals(cOther.metadata) &&
                this.integrity.equals(cOther.integrity) &&
                Arrays.equals(this.data, cOther.data)
        );
    }

}
