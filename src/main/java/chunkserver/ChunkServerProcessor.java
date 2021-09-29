package chunkserver;

import messaging.*;
import networking.Client;
import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
        log.info("Processing {} Message from {}:\n{}", message.getType(), message.getHostname(), message);

        switch(message.getType()) {
            case CHUNK_STORE_REQUEST:
                processChunkStoreRequest((ChunkStoreRequest) message);
                break;
            case CHUNK_STORE_RESPONSE:
                processChunkStoreResponse((ChunkStoreResponse) message);
                break;
            case CHUNK_READ_REQUEST:
                processChunkReadRequest((ChunkReadRequest) message);
                break;
            case CHUNK_REPLACEMENT_REQUEST:
                processChunkReadRequest((ChunkReplacementRequest) message);
                break;
            case CHUNK_REPLACEMENT_RESPONSE:
                processChunkReplacementResponse((ChunkReplacementResponse) message);
                break;
            case CHUNK_REPLICATE_COMMAND:
                processChunkReplicateCommand((ChunkReplicateCommand) message);
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
        ChunkStoreResponse response;

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
                response = (ChunkStoreResponse) MessageFactory.getInstance().createMessage(dataInputStream);
                forwardSocket.close(); // done talking with upstream Chunk Server

                // Process the ChunkStoreResponse from upstream
                if (!response.getSuccess()) { // Just forward the same failure message back, so we can locate the failure
                    log.error("Forwarding back ChunkStoreResponse for file {}, chunk {} failure from Chunk Server {}",
                            message.getAbsoluteFilePath(), message.getSequence(), message.getHostname());
                    sendResponse(this.socket, message);

                } else { // Rebuild same success response message but with our hostname/IP
                    ChunkStoreResponse ourResponse = new ChunkStoreResponse(Host.getHostname(), Host.getIpAddress(),
                            Constants.CHUNK_SERVER_PORT, message.getAbsoluteFilePath(), message.getSequence(),
                            response.getSuccess());
                    log.info("Sending success back to {}: {}\"", this.socket.getInetAddress().getHostName(), message);
                    sendResponse(this.socket, ourResponse);
                }

            } catch (IOException e) {
                log.error("Failed to forward ChunkStoreRequest to Chunk Server {}: {}", nextRecipientHostname, e.getMessage());
                response = new ChunkStoreResponse(Host.getHostname(), Host.getIpAddress(), Constants.CHUNK_SERVER_PORT,
                        message.getAbsoluteFilePath(), message.getSequence(), false);

                sendResponse(this.socket, response);
            }
        } else {
            log.info("We are the last recipient of the ChunkStoreRequest, no need to forward");
            response = new ChunkStoreResponse(Host.getHostname(), Host.getIpAddress(),
                    Constants.CHUNK_SERVER_PORT, message.getAbsoluteFilePath(), message.getSequence(), true);

            // If we've made it here, success; send successful ChunkStoreResponse Message
            log.info("Sending ChunkStoreResponse success back to {}: {}", message.getHostname(), response);
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

    }

    /**
     * Processes a ChunkReadRequest. Attempts to read the requested Chunk from file, and verify its integrity.
     * If found valid, a ChunkReadResponse (or ChunkReplacementResponse) is sent back to the Client or Chunk Server
     * with the loaded Chunk. If found invalid, a HeartbeatMinor is triggered to the Controller, notifying it of
     * chunk corruption, and requesting the hostname an alternative Chunk Server holding a replica of the corrupted
     * chunk. Once a response is received, a ChunkReplacementRequest is sent to the replica Chunk Server for its Chunk.
     * Upon receiving the request, that Chunk Server will attempt this same exact process with its chunk file, but
     * using a ChunkReplacementResponse as response type. The original Chunk Server, upon receiving that
     * ChunkReplacementResponse, will then update its on-disk chunk data, and notify the Controller that it has done
     * so via a ChunkCorrectionNotification. Finally, the correct Chunk is returned to the original Client via a
     * ChunkReadResponse message, along with all the hosts which failed their chunk integrity checks.
     * @param message ChunkReadRequest of a Chunk
     */
    public void processChunkReadRequest(ChunkReadRequest message) {
        String absolutePath = message.getAbsoluteFilePath();
        Integer sequence = message.getSequence();
        ChunkFilename chunkFilename = new ChunkFilename(absolutePath, Chunk.getChunkDir(), sequence);
        boolean requestIsFromClient = message.getType() == Message.MessageType.CHUNK_READ_REQUEST;
        List<String> chunkReplacements = new ArrayList<>();

        // Load chunk from disk
        Chunk requestedChunk = null;
        try {
            requestedChunk = Chunk.load(chunkFilename);
        } catch (IOException e) {
            log.error("Unable to load requested Chunk {}: {}", chunkFilename, e.getMessage());
        }

        // Check validity of chunk
        if (requestedChunk != null && requestedChunk.isValid()) {
            log.info("Chunk {} is valid", chunkFilename); // nothing more to do, just send Chunk back
        } else { // chunk is invalid; get replacement
            log.info("Chunk {} found to be invalid; retrieving replacement...", chunkFilename);

            // Send HeartbeatMinor Message to Controller, notifying it of chunk corruption and requesting a
            // contact for replacement
            HeartbeatMinor chunkCorruptionHeartbeat = new HeartbeatMinor(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    Constants.CHUNK_SERVER_PORT,
                    getChunkServer().discoverChunks().size(),
                    getChunkServer().discoverFreeSpaceAvailable(),
                    new ArrayList<>(), // newlyAddedChunks
                    new ArrayList<>(List.of(new ChunkMetadata(absolutePath, sequence))) // corruptedChunks
            );
            log.info("Getting replication information for chunk {} from Controller", chunkFilename);

            ChunkReplicationInfo criResponse;
            try {
                Socket clientSocket = Client.sendMessage(getChunkServer().controllerHostname,
                        getChunkServer().getControllerPort(), chunkCorruptionHeartbeat);
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                criResponse = (ChunkReplicationInfo) MessageFactory.getInstance().createMessage(dataInputStream);
                clientSocket.close(); // done talking to Controller
                log.info("Received replication information for chunk {} from Controller: {}", chunkFilename, criResponse);
            } catch (IOException e) {
                log.error("Unable to communicate with Controller for chunk replacement: {}", e.getMessage());
                return;
            }

            String contact = criResponse.getReplicationChunkServer();

            // Send ChunkReplacementRequest Message to other Chunk Server, requesting a new valid chunk
            ChunkReplacementRequest replacementRequest = new ChunkReplacementRequest(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    Constants.CHUNK_SERVER_PORT,
                    absolutePath,
                    sequence
            );
            log.info("Requesting replacement for chunk {} from Chunk Server {}", chunkFilename, contact);

            ChunkReplacementResponse crrResponse;
            try {
                Socket clientSocket = Client.sendMessage(contact, Constants.CHUNK_SERVER_PORT, replacementRequest);
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                crrResponse = (ChunkReplacementResponse) MessageFactory.getInstance().createMessage(dataInputStream);
                clientSocket.close(); // done talking to replacement Chunk Server
                log.info("Received replacement for chunk {} from Chunk Server {}: {}", chunkFilename, contact,
                        crrResponse);
            } catch (IOException e) {
                log.error("Unable to retrieve replacement for chunk {} from Chunk Server{}: {}", chunkFilename, contact,
                        e.getMessage());
                return;
            }

            // Replace our invalid stored chunk with valid Chunk from replacement Chunk Server
            try {
                Chunk.save(crrResponse.getChunk(), chunkFilename);
                log.info("Successfully saved valid chunk replacement {} to storage", chunkFilename);
            } catch (IOException e) {
                log.error("Unable to replace invalid chunk {}: {}", chunkFilename, e.getMessage());
                return;
            }

            // Notify the Controller of the chunk correction; not fatal if try fails
            try {
                ChunkCorrectionNotification correctionNotification = new ChunkCorrectionNotification(
                        Host.getHostname(),
                        Host.getIpAddress(),
                        Constants.CHUNK_SERVER_PORT,
                        absolutePath,
                        sequence
                );
                Socket clientSocket = Client.sendMessage(getChunkServer().getControllerHostname(),
                        Constants.CONTROLLER_PORT, correctionNotification);
                clientSocket.close(); // we are not expecting a response
            } catch (IOException e) {
                log.warn("Unable to notify Controller of chunk {} correction: {}", chunkFilename, e.getMessage());
            }

            // Record ourselves as one of the Chunk Servers that had to invoke a replacement procedure,
            // along with all Chunk Servers who reported the same upstream
            chunkReplacements.add(Host.getHostname());
            chunkReplacements.addAll(crrResponse.getChunkReplacements());

            // Update Chunk for response with fixed/validated replacement Chunk
            requestedChunk = crrResponse.getChunk();
        }

        // Send response: ChunkReadResponse if responding to Client; ChunkReplacementResponse if responding to Chunk Server
        ChunkReadResponse response;
        if (requestIsFromClient) {
            response = new ChunkReadResponse(Host.getHostname(), Host.getIpAddress(), Constants.CHUNK_SERVER_PORT,
                    absolutePath, sequence, requestedChunk, chunkReplacements);
        } else {
            response = new ChunkReplacementResponse(Host.getHostname(), Host.getIpAddress(),
                    Constants.CHUNK_SERVER_PORT, absolutePath, sequence, requestedChunk, chunkReplacements);
        }
        log.info("Sending {} back to {}: {}", message.getType(), message.getHostname(), response);
        sendResponse(this.socket, response);
    }

    /**
     * Invoked when the Controller has chosen us to store the replica of a chunk lost in a Chunk Server failure.
     * When received unprovoked, it will be from another Chunk Server sending us a valid copy of the chunk
     * as instructed by the Controller. Once we've stored the Chunk, we send a ChunkCorrectionNotification
     * to the Controller, letting it know we've successfully stored the replica.
     * @param message ChunkReplacementResponse Message containing the Chunk we need to store.
     */
    public void processChunkReplacementResponse(ChunkReplacementResponse message) {
        ChunkFilename chunkFilename = new ChunkFilename(message.getAbsoluteFilePath(), Chunk.getChunkDir(),
                message.getSequence());

        // Save chunk
        try {
            Chunk.save(message.getChunk(), chunkFilename);
        } catch (IOException e) {
            log.error("Unable to save chunk {}: {}", chunkFilename, e.getMessage());
            return;
        }

        // Notify Controller of successful chunk replication
        try {
            ChunkCorrectionNotification notification = new ChunkCorrectionNotification(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    Constants.CHUNK_SERVER_PORT,
                    message.getAbsoluteFilePath(),
                    message.getSequence());

            Socket clientSocket = Client.sendMessage(
                    getChunkServer().getControllerHostname(),
                    getChunkServer().getControllerPort(),
                    notification);
            clientSocket.close(); // done talking to Controller
            log.info("Successfully sent ChunkCorrectionNotification to Controller");
        } catch (IOException e) {
            log.error("Unable to send Controller ChunkCorrectionNotification! {}", e.getMessage());
        }
    }

    /**
     * Processes a ChunkReplicateCommand from the Controller, telling us to share a chunk we host with another
     * Chunk Server for replication. This happens when a Chunk Server fails or a chunk is lost, and replication
     * levels for chunks need to be restored.
     * @param message ChunkReplicateCommand the command from the Controller telling us which chunk we need to share,
     *                and the target Chunk Server we need to share it with.
     */
    public void processChunkReplicateCommand(ChunkReplicateCommand message) {
        ChunkFilename chunkFilename = new ChunkFilename(message.getAbsoluteFilePath(), Chunk.getChunkDir(),
                message.getSequence());

        // Load Chunk for replication
        Chunk chunkForReplication;
        try {
            log.info("Attempting to load chunk {}", chunkFilename);
            chunkForReplication = Chunk.load(chunkFilename);
        } catch (IOException e) {
            log.error("Unable to load chunk {}: {}", chunkFilename, e.getMessage());
            return;
        }

        // Send Chunk for replication to target Chunk Server
        try {
            ChunkReplacementResponse request = new ChunkReplacementResponse(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    Constants.CHUNK_SERVER_PORT,
                    message.getAbsoluteFilePath(),
                    message.getSequence(),
                    chunkForReplication,
                    new ArrayList<>()
            );

            Socket clientSocket = Client.sendMessage(message.getTargetChunkServer(), Constants.CHUNK_SERVER_PORT,
                    request);
            clientSocket.close(); // done talking to other Chunk Server
            log.info("Successfully sent ChunkReplacementResponse to {}", message.getTargetChunkServer());
        } catch (IOException e) {
            log.error("Unable to make ChunkReplacementResponse request to {}: {}", message.getTargetChunkServer(),
                    e.getMessage());
        }
    }

}
