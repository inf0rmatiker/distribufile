package client;

import chunkserver.ChunkServerProcessor;
import messaging.ClientWriteRequest;
import messaging.ClientWriteResponse;
import messaging.Message;
import messaging.MessageFactory;
import networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Host;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class FileClient extends Client {

    public static Logger log = LoggerFactory.getLogger(FileClient.class);

    public String controllerHostname;
    public Integer controllerPort;

    public FileClient(String controllerHostname, Integer controllerPort) {
        this.controllerHostname = controllerHostname;
        this.controllerPort = controllerPort;
    }

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
    public void writeFile(String absolutePath) throws IOException {
        log.info("Writing file {}", absolutePath);
        FileLoader loader = new FileLoader(absolutePath);

        byte[] chunkRead = loader.readChunk();
        int sequence = 0;
        while (chunkRead != null) {

            // Construct and send ClientWriteRequest for chunk
            ClientWriteRequest writeRequest = new ClientWriteRequest(Host.getHostname(), Host.getIpAddress(), 0,
                    absolutePath, sequence);
            Socket clientSocket = Client.sendMessage(this.controllerHostname, this.controllerPort, writeRequest);

            // Wait for response and process it
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            Message response = MessageFactory.getInstance().createMessage(dataInputStream);
            processResponse(response);

            // Read next chunk and increment chunk sequence index
            chunkRead = loader.readChunk();
            sequence++;
        }

        // Clean up file reader
        loader.close();
    }

    @Override
    public void processResponse(Message message) {
        log.info("Processing {} Message Response:\n{}", message.getType(), message);

        switch (message.getType()) {
            case CLIENT_WRITE_RESPONSE:
                break;
            default:
                log.error("Unknown MessageType {}", message.getType());
        }

    }

    public void processClientWriteResponse(ClientWriteResponse message) {

    }
}
