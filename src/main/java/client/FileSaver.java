package client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Saves a file to the Client's filesystem. Allows buffered writing as to avoid
 * memory overhead.
 */
public class FileSaver {

    Logger log = LoggerFactory.getLogger(FileSaver.class);

    public String absolutePath;
    private final BufferedOutputStream writer;

    /**
     * Constructor, creates/opens a new File from absolutePath, and a BufferedOutputStream to it
     * @param absolutePath The absolute path of the file we are going to write to
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file,
     *  does not exist but cannot be created, or cannot be opened for any other reason
     */
    public FileSaver(String absolutePath) throws FileNotFoundException {
        this.absolutePath = absolutePath;

        // Create new file from absolutePath, and a BufferedOutputStream from it
        log.info("Opening file \"{}\" for writing...", absolutePath);
        File file = new File(absolutePath);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        this.writer = new BufferedOutputStream(fileOutputStream, Constants.CHUNK_SIZE);
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Writes a chunk to the File maintained by the BufferedOutputStream writer.
     * @param chunk An array of bytes
     * @throws IOException
     */
    public void writeChunk(byte[] chunk) throws IOException {
        this.writer.write(chunk);
        this.writer.flush();
        log.info("Wrote {}-byte chunk to file \"{}\"", chunk.length, absolutePath);
    }

    /**
     * Closes the BufferedOutputStream writer, releasing the file resource back to the system.
     * @throws IOException
     */
    public void close() throws IOException {
        log.info("Finished writing to file \"{}\"", absolutePath);
        this.writer.close();
    }

}
