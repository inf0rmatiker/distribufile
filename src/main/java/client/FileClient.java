package client;

import messaging.*;
import networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

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
     * @param outputFile Path of the file, relative or absolute, that we want to save the retrieved file to
     */
    public void readFile(String absolutePath, String outputFile) throws IOException {
        ClientReadRequest readRequest = new ClientReadRequest(Host.getHostname(), Host.getIpAddress(), 0, absolutePath);
        Socket clientSocket = sendMessage(this.controllerHostname, this.controllerPort, readRequest);

        // Wait for response and process it
        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
        Message response = MessageFactory.getInstance().createMessage(dataInputStream);
        log.info("Received {} Message: {}", response.getType(), response);

        processClientReadResponse((ClientReadResponse) response, outputFile);
    }

    /**
     * Writes a file to the distributed file system:
     * 1. Opens a file for buffered reading, reading one chunk at a time
     * 2. For each chunk of data read, asks the Controller which Chunk Servers the chunk should be replicated on
     * 3. Once the list of Chunk Servers has been obtained from the Controller, reaches out to the first Chunk Server
     *    in the list with a ChunkStoreRequest, which is then forwarded by that Chunk Server to the next, and so on.
     * 4. Waits for a ChunkStoreResponse to assert the success/failure of that chunk storage.
     * 5. Repeat steps 2, 3, 4 for each chunk in the file.
     * @param absolutePath String absolute path of the file we are writing
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
            Socket clientSocket = sendMessage(this.controllerHostname, this.controllerPort, writeRequest);

            // Wait for response and process it
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            Message response = MessageFactory.getInstance().createMessage(dataInputStream);
            log.info("Received {} Message: {}", response.getType(), response);
            processClientWriteResponse((ClientWriteResponse) response, chunkRead);

            // Read next chunk and increment chunk sequence index
            chunkRead = loader.readChunk();
            sequence++;
        }

        // Clean up file reader
        loader.close();
    }

    /**
     * Processes a ClientWriteResponse from the Controller, containing
     * a list of Chunk Servers to write the Chunk to.
     * @param message ClientWriteResponse Message received from the Controller
     * @throws IOException If unable to read message or send message
     */
    public void processClientWriteResponse(ClientWriteResponse message, byte[] chunk) throws IOException {
        List<String> chunkServers = message.getReplicationChunkServers();
        String poppedChunkServer = chunkServers.remove(chunkServers.size() - 1);
        ChunkStoreRequest request = new ChunkStoreRequest(Host.getHostname(), Host.getIpAddress(), 0,
                chunkServers, message.getAbsoluteFilePath(), message.getSequence(), chunk);
        Socket clientSocket = sendMessage(poppedChunkServer, Constants.CHUNK_SERVER_PORT, request);

        // Wait for response and process it
        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
        Message response = MessageFactory.getInstance().createMessage(dataInputStream);
        log.info("Received {} Message: {}", response.getType(), response);
    }

    /**
     * Processes a ClientReadResponse from the Controller, containing a list of Chunk Servers with
     * the chunks of the file we are trying to read.
     * @param message ClientReadResponse Message received from the Controller
     * @throws IOException If unable to read message or send message
     */
    public void processClientReadResponse(ClientReadResponse message, String outputFile) throws IOException {
        if (message.getFileExists()) {
            String filename = message.getAbsoluteFilePath();
            FileSaver fileSaver = new FileSaver(outputFile);

            for (int sequence = 0; sequence < message.getChunkServerHostnames().size(); sequence++) {
                String chunkServerHostname = message.getChunkServerHostnames().get(sequence);
                log.info("Requesting chunk sequence {} from Chunk Server {}", sequence, chunkServerHostname);

                ChunkReadRequest readRequest = new ChunkReadRequest(Host.getHostname(), Host.getIpAddress(), 0,
                        filename, sequence);
                Socket clientSocket = sendMessage(chunkServerHostname, Constants.CHUNK_SERVER_PORT, readRequest);

                // Wait for response and process it
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                Message response = MessageFactory.getInstance().createMessage(dataInputStream);
                log.info("Received {} Message: {}", response.getType(), response);
                processChunkReadResponse((ChunkReadResponse) response, fileSaver);
            }

            log.info("Wrote all {} chunks to {}", message.getChunkServerHostnames().size(), filename);
            fileSaver.close();
        } else {
            // TODO: Handle Controller saying file does not exist
        }
    }

    /**
     * Processes a ChunkReadResponse from a Chunk Server, containing the (hopefully) verified chunk data
     * for a given chunk.
     * @param message ChunkReadResponse from ChunkServer
     * @param fileSaver FileSaver reference to write our chunks to disk
     * @throws IOException If unable to write to disk
     */
    public void processChunkReadResponse(ChunkReadResponse message, FileSaver fileSaver) throws IOException {
        if (message.getIntegrityVerified()) {
            log.info("Integrity verified for chunk {} of file {}, saving to disk...", message.getSequence(),
                    message.getAbsoluteFilePath());
            fileSaver.writeChunk(message.getChunk().data);
        }
    }
}
