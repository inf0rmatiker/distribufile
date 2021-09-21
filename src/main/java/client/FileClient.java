package client;

import messaging.Message;
import networking.Client;

public class FileClient extends Client {

    /**
     * Retrieves an entire file from the distributed file system, and saves it to client's disk:
     * 1. Reaches out to the Controller server to retrieve a list of chunks, and from which Chunk Servers to retrieve them.
     * 2. For each of the chunks in the list, reaches out to the holding Chunk Server and retrieves the chunk.
     * 3. Appends the chunk data to the file.
     * 4. Repeat 2, 3 until all chunks have been retrieved and stored in the correct sequence.
     * @param absolutePath Absolute path of the file, from the client's perspective, that exists on the distributed FS.
     */
    public void readFile(String absolutePath) {

    }

    /**
     * Writes a file to the distributed file system:
     * 1. Opens a file for buffered reading, reading one chunk at a time
     * 2. For each chunk of data read, asks the Controller which Chunk Servers the chunk should be replicated on
     * 3. Once the list of Chunk Servers has been obtained from the Controller, reaches out to the first Chunk Server
     *    in the list with a ChunkStoreRequest, which is then forwarded by that Chunk Server to the next, and so on.
     * 4. Waits for a ChunkStoreResponse to assert the success/failure of that chunk storage.
     * 5. Repeat steps 2, 3, 4 for each chunk in the file.
     * @param absolutePath
     */
    public void writeFile(String absolutePath) {

    }

    @Override
    public void processResponse(Message message) {

    }
}
