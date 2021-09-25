package controller;

import chunkserver.ChunkMetadata;
import messaging.*;
import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class ControllerProcessor extends Processor {

    public static Logger log = LoggerFactory.getLogger(ControllerProcessor.class);

    public Controller controller;

    public ControllerProcessor(Socket socket, Controller controller) {
        this.controller = controller;
        this.socket = socket;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public void process(Message message) {
        // TODO: Implement all possible Message request/response types for Controller
        log.info("Processing {} Message from {}", message.getType(), message.getHostname());

        switch (message.getType()) {
            case CLIENT_WRITE_REQUEST:
                processClientWriteRequest((ClientWriteRequest) message);
                break;
            case HEARTBEAT_MAJOR:
                processHeartbeatMajor((HeartbeatMajor) message);
                break;
            case HEARTBEAT_MINOR:
                processHeartbeatMinor((HeartbeatMinor) message);
                break;
            default:
                log.error("Unimplemented Message type \"{}\"", message.getType());
        }
    }

    /**
     * Uses the HeartbeatMajor Message to replace the metadata of a Chunk Server with fresh metadata
     * @param message HeartbeatMajor Message containing metadata about a Chunk Server
     */
    public void processHeartbeatMajor(HeartbeatMajor message) {
        Vector<ChunkMetadata> chunkMetadata = new Vector<>(message.getChunksMetadata()); // copy into Vector
        ChunkServerMetadata csm = new ChunkServerMetadata(message.getHostname(), message.getFreeSpaceAvailable(),
                message.getTotalChunksMaintained(), chunkMetadata);

        this.controller.replaceChunkServerMetadata(csm);
        this.controller.updateFilesMetadata(chunkMetadata, message.getHostname());
    }

    /**
     * Uses the HeartbeatMinor Message to update the metadata of a Chunk Server
     * @param message HeartbeatMinor Message containing metadata about a Chunk Server
     */
    public void processHeartbeatMinor(HeartbeatMinor message) {
        Vector<ChunkMetadata> chunkMetadata = new Vector<>(message.getNewlyAddedChunks());
        ChunkServerMetadata csm = new ChunkServerMetadata(message.getHostname(), message.getFreeSpaceAvailable(),
                message.getTotalChunksMaintained(), chunkMetadata);

        this.controller.updateChunkServerMetadata(csm);
        this.controller.updateFilesMetadata(chunkMetadata, message.getHostname());
    }

    /**
     * Processes a request from a Client to write a single chunk of a file.
     *
     * If this file/chunk has never been seen before, adds the appropriate file metadata
     * information for tracking, determines which Chunk Servers should store the chunk replicas,
     * and responds to the Client with a ClientWriteResponse containing this list.
     *
     * If this file/chunk is currently being tracked, we treat this as an update request.
     * Determine the Chunk Servers which currently store this chunk's replicas, and respond to the Client with a
     * ClientWriteResponse containing this list.
     *
     * @param message ClientWriteRequest Message containing the filename the chunk belongs to,
     *                and the chunk sequence index.
     */
    public void processClientWriteRequest(ClientWriteRequest message) {

        String filename = message.getAbsoluteFilePath();
        int sequence = message.getSequence();
        Set<String> replicationChunkServers;
        boolean alreadyTrackingFile = getController().getFilesMetadata().containsKey(filename);

        if (alreadyTrackingFile) {
            FileMetadata fileMetadata = getController().getFilesMetadata().get(filename);
            Set<String> chunkServers = fileMetadata.get(sequence);

            if (chunkServers == null || chunkServers.isEmpty()) {

                // Select Chunk Servers to use for chunk replication, and add selections to FileMetadata for tracking
                replicationChunkServers = getController().selectBestChunkServersForReplicas();
                fileMetadata.put(replicationChunkServers, sequence);
            } else {

                // Just use preexisting Chunk Server hostnames for response
                replicationChunkServers = fileMetadata.getChunkServerHostnames().get(sequence);
            }
        } else { // we are not already tracking file; need to do so

            // Create new FileMetadata, select Chunk Servers for chunk replication, then track
            FileMetadata fileMetadata = new FileMetadata(filename);
            replicationChunkServers = getController().selectBestChunkServersForReplicas();
            fileMetadata.put(replicationChunkServers, sequence);
            getController().getFilesMetadata().put(filename, fileMetadata);
        }

        // Construct response message and send it back to client
        ClientWriteResponse clientWriteResponse = new ClientWriteResponse(Host.getHostname(), Host.getIpAddress(),
                Constants.CONTROLLER_PORT, new ArrayList<>(replicationChunkServers), filename, sequence);
        sendResponse(this.socket, clientWriteResponse);
    }
}
