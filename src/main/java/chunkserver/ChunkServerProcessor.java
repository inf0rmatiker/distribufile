package chunkserver;

import messaging.ChunkStoreRequest;
import messaging.Message;
import networking.Client;
import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.*;
import java.net.Socket;

public class ChunkServerProcessor extends Processor {

    public static Logger log = LoggerFactory.getLogger(ChunkServerProcessor.class);

    // Reference to ChunkServer
    public ChunkServer chunkServer;

    public ChunkServerProcessor(Socket socket, ChunkServer chunkServer) {
        this.chunkServer = chunkServer;
        this.socket = socket;
    }

    public ChunkServer getChunkServer() {
        return chunkServer;
    }

    @Override
    public void processRequest(Message message) {
        // TODO: Implement all possible Message request types
        log.info("Processing {} Message", message.getType());

        try {
            switch(message.getType()) {
                case CHUNK_STORE_REQUEST:
                    processChunkStoreRequest((ChunkStoreRequest) message);
                    break;
                default: log.error("Unimplemented Message type \"{}\"", message.getType());
            }
        } catch (IOException e) {
            log.error("Caught IOException in ChunkServerProcessor::processRequest(): {}", e.getMessage());
        }

    }

    /**
     * Processes a ChunkStoreRequest Message -- If the chunk already exists on disk, it is updated with the new chunk
     * data in the message and its version is incremented. Otherwise, the chunk data in the message is saved for the
     * first time with a version of 1. Finally, if there are more chunk servers to replicate the chunk on, the message
     * is forwarded to the next one and that chunk server is removed from the list of forward recipients.
     * @param message ChunkStoreRequest message.
     * @throws IOException If unable to forward a message, calculate checksums, update or save the chunk data.
     */
    public void processChunkStoreRequest(ChunkStoreRequest message) throws IOException {

        // Build in-memory Chunk from message
        ChunkFilename filename = new ChunkFilename(message.getAbsoluteFilePath(), Chunk.getChunkDir(), message.getSequence());
        ChunkMetadata metadata = new ChunkMetadata(message.getAbsoluteFilePath(), message.getSequence(), message.getChunkData().length);
        ChunkIntegrity integrity = new ChunkIntegrity(ChunkIntegrity.calculateSliceChecksums(message.getChunkData()));
        Chunk chunk = new Chunk(metadata, integrity, message.getChunkData());

        // Either save or update chunk file
        if (Chunk.alreadyExists(filename)) {
            log.info("Chunk {} already exists, updating it", filename);
            Chunk.update(chunk, filename);
        } else {
            log.info("Chunk {} does not already exist, saving it for the first time", filename);
            Chunk.save(chunk, filename);
        }

        // Forward message if there's more recipients
        if (!message.getReplicationChunkServers().isEmpty()) {
            String nextRecipientHostname = message.popReplicationRecipient();
            log.info("Forwarding ChunkStoreRequest Message to next Chunk Server: {}", nextRecipientHostname);

            // Re-marshal message with one less recipient
            message.marshal();

            // Send message to next chunk server
            Socket socket = Client.sendMessage(nextRecipientHostname, Constants.CHUNK_SERVER_PORT, message);
            socket.close();
        } else {
            log.info("We are the last recipient of the ChunkStoreRequest, no need to forward");
        }
    }

}
