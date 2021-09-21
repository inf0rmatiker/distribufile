package chunkserver;

import messaging.ChunkStoreRequest;
import messaging.ChunkStoreResponse;
import messaging.Message;
import messaging.MessageFactory;
import networking.Client;
import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

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
    public void process(Message message) {
        // TODO: Implement all possible Message request/response types for ChunkServer
        log.info("Processing {} Message:\n{}", message.getType(), message);


        switch(message.getType()) {
            case CHUNK_STORE_REQUEST:
                processChunkStoreRequest((ChunkStoreRequest) message);
                break;
            case CHUNK_STORE_RESPONSE:
                processChunkStoreResponse((ChunkStoreResponse) message);
                break;
            default: log.error("Unimplemented Message type \"{}\"", message.getType());
        }


    }

    /**
     * Processes a ChunkStoreRequest Message -- If the chunk already exists on disk, it is updated with the new chunk
     * data in the message and its version is incremented. Otherwise, the chunk data in the message is saved for the
     * first time with a version of 1. Finally, if there are more chunk servers to replicate the chunk on, the message
     * is forwarded to the next one and that chunk server is removed from the list of forward recipients.
     * @param message ChunkStoreRequest message.
     */
    public void processChunkStoreRequest(ChunkStoreRequest message) {

        // Build in-memory Chunk from message
        ChunkFilename filename = new ChunkFilename(message.getAbsoluteFilePath(), Chunk.getChunkDir(), message.getSequence());
        ChunkMetadata metadata = new ChunkMetadata(message.getAbsoluteFilePath(), message.getSequence(), message.getChunkData().length);
        ChunkIntegrity integrity = new ChunkIntegrity(ChunkIntegrity.calculateSliceChecksums(message.getChunkData()));
        Chunk chunk = new Chunk(metadata, integrity, message.getChunkData());
        Message response;

        // Either save or update chunk file
        try {
            if (Chunk.alreadyExists(filename)) {
                log.info("Chunk {} already exists, updating it", filename);
                Chunk.update(chunk, filename);
            } else {
                log.info("Chunk {} does not already exist, saving it for the first time", filename);
                Chunk.save(chunk, filename);
            }
        } catch (IOException e) {
            log.error("Failed to save or update chunk {}: {}", filename, e.getMessage());
            response = new ChunkStoreResponse(Host.getHostname(), Host.getIpAddress(), Constants.CHUNK_SERVER_PORT,
                    message.getAbsoluteFilePath(), message.getSequence(), false);

            sendResponse(this.socket, response);
            return; // Fail fast, don't attempt to forward request
        }

        // Successfully saved/updated Chunk, attempt to forward request if there's more recipients
        if (!message.getReplicationChunkServers().isEmpty()) {
            String nextRecipientHostname = message.popReplicationRecipient();
            log.info("Forwarding ChunkStoreRequest Message to next Chunk Server: {}", nextRecipientHostname);

            try {
                // Re-marshal message with one less recipient
                message.marshal();

                // Send message to next chunk server
                Socket forwardSocket = Client.sendMessage(nextRecipientHostname, Constants.CHUNK_SERVER_PORT, message);

                // Wait for ChunkStoreResponse from forward recipient
                DataInputStream dataInputStream = new DataInputStream(forwardSocket.getInputStream());
                response = MessageFactory.getInstance().createMessage(dataInputStream);

                // Process the ChunkStoreResponse from upstream
                process(response);

                // Close our open socket with forward recipient; we are done talking with them
                forwardSocket.close();

            } catch (IOException e) {
                log.error("Failed to forward ChunkStoreRequest to Chunk Server {}: {}", nextRecipientHostname, e.getMessage());
                response = new ChunkStoreResponse(Host.getHostname(), Host.getIpAddress(), Constants.CHUNK_SERVER_PORT,
                        message.getAbsoluteFilePath(), message.getSequence(), false);

                sendResponse(this.socket, response);
                return; // Fail fast, don't attempt to process upstream response
            }


        } else {
            log.info("We are the last recipient of the ChunkStoreRequest, no need to forward");
            response = new ChunkStoreResponse(Host.getHostname(), Host.getIpAddress(),
                    Constants.CHUNK_SERVER_PORT, message.getAbsoluteFilePath(), message.getSequence(), true);

            // If we've made it here, success; send successful ChunkStoreResponse Message
            log.info("Sending ChunkStoreResponse SUCCESS back to {}: {}", message.getHostname(), response);
            sendResponse(this.socket, response);
        }
    }

    /**
     * Processes a ChunkStoreResponse: If we receive a failure Message from upstream, just forward that back to
     * the Socket connection that triggered the original request. If we receive success from upstream, then rebuild
     * a success ChunkStoreResponse Message to send back to the Socket connection that stored the original request.
     * @param message The ChunkStoreResponse message received from upstream (a forward ChunkServer recipient of a
     *                ChunkStoreRequest)
     */
    public void processChunkStoreResponse(ChunkStoreResponse message) {
        if (!message.getSuccess()) { // Just forward the same failure message back, so we can locate the failure
            log.error("Forwarding back ChunkStoreResponse for file {}, chunk {} FAILURE from Chunk Server {}",
                    message.getAbsoluteFilePath(), message.getSequence(), message.getHostname());
            sendResponse(this.socket, message);
        } else { // Rebuild same success response message but with our hostname/IP
            ChunkStoreResponse ourResponse = new ChunkStoreResponse(Host.getHostname(), Host.getIpAddress(),
                    Constants.CHUNK_SERVER_PORT, message.getAbsoluteFilePath(), message.getSequence(),
                    message.getSuccess());
            log.info("Sending SUCCESS back to {}: {}\"", this.socket.getInetAddress().getHostName(), message);
            sendResponse(this.socket, ourResponse);
        }
    }

}
