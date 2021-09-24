package client;

import messaging.HeartbeatMajor;
import util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Loads a file from the client's filesystem, breaking it into chunks of a specific size.
 * The final chunk may be less than the specified chunk size, due to internal fragmentation.
 */
public class FileLoader {

    Logger log = LoggerFactory.getLogger(FileLoader.class);

    public String absolutePath;
    private final BufferedInputStream reader;

    /**
     * Constructor, opens a BufferedInputStream on the specified file.
     * @param absolutePath The absolute path to the file we want to load.
     * @throws FileNotFoundException If the file isn't there.
     */
    public FileLoader(String absolutePath) throws FileNotFoundException {
        this.absolutePath = absolutePath;

        // Create BufferedInputStream from file with buffer size same as chunk size
        FileInputStream fileInputStream = new FileInputStream(absolutePath);
        this.reader = new BufferedInputStream(fileInputStream, Constants.CHUNK_SIZE);
    }

    /**
     * Reads a chunk of the file from the BufferedInputStream into a byte array.
     * @return The next chunk of the file in the form of a byte array.
     * @throws IOException
     */
    public byte[] readChunk() throws IOException {
        byte[] chunk = new byte[Constants.CHUNK_SIZE];
        int bytesRead = this.reader.read(chunk, 0, Constants.CHUNK_SIZE);

        if (bytesRead == -1) {
            log.info("Finished reading final chunk of file \"{}\"", this.absolutePath);
            return null;
        } else if (bytesRead < Constants.CHUNK_SIZE) {
            byte[] resizedChunk = new byte[bytesRead];
            System.arraycopy(chunk, 0, resizedChunk, 0, resizedChunk.length);
            chunk = resizedChunk;
        }

        log.info("Read chunk of size {} bytes", chunk.length);
        return chunk;
    }

    /**
     * Closes the BufferedInputStream reader, releasing the file resource back to the system.
     * @throws IOException
     */
    public void close() throws IOException {
        this.reader.close();
    }
}
