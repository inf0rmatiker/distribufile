package controller;

import chunkserver.ChunkMetadata;
import messaging.*;
import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.net.Socket;
import java.util.*;

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
        log.info("Processing {} Message from {}", message.getType(), message.getHostname());

        switch (message.getType()) {
            case CLIENT_WRITE_REQUEST:
                processClientWriteRequest((ClientWriteRequest) message);
                break;
            case CLIENT_READ_REQUEST:
                processClientReadRequest((ClientReadRequest) message);
                break;
            case HEARTBEAT_MAJOR:
                processHeartbeatMajor((HeartbeatMajor) message);
                break;
            case HEARTBEAT_MINOR:
                processHeartbeatMinor((HeartbeatMinor) message);
                break;
            case CHUNK_CORRECTION_NOTIFICATION:
                processChunkCorrectionNotification((ChunkCorrectionNotification) message);
                break;
            case SYSTEM_REPORT_REQUEST:
                processSystemReportRequest((SystemReportRequest) message);
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

        // Log the chunks stored by the sender
        StringBuilder sb = new StringBuilder(String.format("Chunks stored by %s:\n", message.getHostname()));
        for (ChunkMetadata cm: message.getChunksMetadata()) {
            sb.append(String.format("\t%s, sequence %d\n", cm.getAbsoluteFilePath(), cm.getSequence()));
        }
        log.info(sb.toString());

        this.controller.replaceChunkServerMetadata(message.getHostname(), message.getFreeSpaceAvailable(),
                message.getTotalChunksMaintained(), message.getChunksMetadata());
        this.controller.updateFilesMetadata(message.getChunksMetadata(), message.getHostname());
    }

    /**
     * Uses the HeartbeatMinor Message to update the metadata of a Chunk Server
     * @param message HeartbeatMinor Message containing metadata about a Chunk Server
     */
    public void processHeartbeatMinor(HeartbeatMinor message) {
        if (message.getCorruptedChunks().isEmpty()) {

            if (!message.getNewlyAddedChunks().isEmpty()) {
                StringBuilder sb = new StringBuilder(String.format("Chunk Server %s received new chunks\n",
                        message.getHostname()));
                for (ChunkMetadata cm: message.getNewlyAddedChunks()) {
                    sb.append(String.format("\t%s, sequence %d\n", cm.getAbsoluteFilePath(), cm.getSequence()));
                }
                log.info(sb.toString());
            }

            this.controller.updateChunkServerMetadata(message.getHostname(), message.getFreeSpaceAvailable(),
                    message.getTotalChunksMaintained(), message.getNewlyAddedChunks());
            this.controller.updateFilesMetadata(message.getNewlyAddedChunks(), message.getHostname());

        } else { // this is a notification of a corrupted chunk
            ChunkMetadata corruptedChunk = message.getCorruptedChunks().get(0);
            FileMetadata fileMetadata = getController().getFilesMetadata().get(corruptedChunk.getAbsoluteFilePath());
            Set<String> replicaChunkServers = fileMetadata.getChunkServerHostnames().get(corruptedChunk.getSequence());

            log.info("Removing {} as Chunk Server host for chunk {}, sequence {} until chunk is corrected",
                    message.getHostname(), corruptedChunk.getAbsoluteFilePath(), corruptedChunk.getSequence());

            replicaChunkServers.remove(message.getHostname());
            String otherReplicaServer = replicaChunkServers.iterator().next();

            log.info("Chunk {}, sequence {} corrupted on Chunk Server {}, suggesting {} for replication",
                    corruptedChunk.getAbsoluteFilePath(), corruptedChunk.getSequence(), message.getHostname(),
                    otherReplicaServer);
            ChunkReplicationInfo response = new ChunkReplicationInfo(Host.getHostname(), Host.getIpAddress(),
                    Constants.CONTROLLER_PORT, otherReplicaServer);
            sendResponse(this.socket, response);
        }
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

            // Tracking file, but not that specific chunk
            if (chunkServers == null || chunkServers.isEmpty()) {

                // Select Chunk Servers to use for chunk replication, and add selections to FileMetadata for tracking
                replicationChunkServers = getController().selectBestChunkServersForReplicas();
                fileMetadata.put(replicationChunkServers, sequence);
                for (String host: replicationChunkServers) {
                    ChunkServerMetadata csm = getController().getChunkServerMetadata().get(host);
                    if (!csm.contains(filename, sequence)) {
                        csm.incrementTotalChunksMaintained();
                    }
                }

            } else {

                // Just use preexisting Chunk Server hostnames for response, since this is an update
                replicationChunkServers = fileMetadata.getChunkServerHostnames().get(sequence);
            }
        } else { // we are not already tracking file; need to do so

            // Create new FileMetadata, select Chunk Servers for chunk replication, then track
            FileMetadata fileMetadata = new FileMetadata(filename);
            replicationChunkServers = getController().selectBestChunkServersForReplicas();
            fileMetadata.put(replicationChunkServers, sequence);
            getController().getFilesMetadata().put(filename, fileMetadata);

            for (String host: replicationChunkServers) {
                ChunkServerMetadata csm = getController().getChunkServerMetadata().get(host);
                if (!csm.contains(filename, sequence)) {
                    csm.incrementTotalChunksMaintained();
                }
            }
        }

        // Construct response message and send it back to client
        ClientWriteResponse clientWriteResponse = new ClientWriteResponse(Host.getHostname(), Host.getIpAddress(),
                Constants.CONTROLLER_PORT, new ArrayList<>(replicationChunkServers), filename, sequence);
        sendResponse(this.socket, clientWriteResponse);
    }

    /**
     * Processes a ClientReadRequest by retrieving the storage locations for each chunk of the file,
     * and sending then back to the client. If the file is unrecognized, flag is indicated in the response.
     * @param message ClientReadRequest containing the filename requested for retrieval
     */
    public void processClientReadRequest(ClientReadRequest message) {
        String filename = message.getAbsoluteFilePath();
        ClientReadResponse response;

        if (getController().getFilesMetadata().containsKey(filename)) {

            log.info("File {} exists, returning Chunk Server hosts for chunks...", filename);
            FileMetadata fileMetadata = getController().getFilesMetadata().get(filename);
            List<String> chunkHostnames = new ArrayList<>();
            for (Set<String> chunkServers: fileMetadata.getChunkServerHostnames()) {
                chunkHostnames.add(chunkServers.iterator().next());
            }

            // Success - found FileMetadata and Chunk Server locations
            response = new ClientReadResponse(Host.getHostname(), Host.getIpAddress(), Constants.CONTROLLER_PORT,
                    filename, chunkHostnames, true);
        } else {

            log.info("File {} does not exist, returning search failure", filename);

            // Failure - unable to find FileMetadata
            response = new ClientReadResponse(Host.getHostname(), Host.getIpAddress(), Constants.CONTROLLER_PORT,
                    filename, new ArrayList<>(), false);
        }
        sendResponse(this.socket, response);
    }

    /**
     * Adds the hostname of the Chunk Server message sender to the set of hosts for a given chunk, telling the
     * Controller that it has received a valid copy of the Chunk and is now hosting it.
     * @param message ChunkCorrectionNotification of a Chunk Server for a given chunk
     */
    public void processChunkCorrectionNotification(ChunkCorrectionNotification message) {
        String filename = message.getAbsoluteFilePath();
        Integer sequence = message.getSequence();

        // Add sender hostname to set of hosts for the chunk
        FileMetadata fileMetadata = getController().getFilesMetadata().get(filename);
        Set<String> chunkServers = fileMetadata.get(sequence);
        chunkServers.add(message.getHostname());
        log.info("{} now storing a valid copy of chunk {}, sequence {} ", message.getHostname(), filename, sequence);
    }

    /**
     * Responds to a Client's request for system status information with a SystemReportResponse message.
     * @param message SystemReportRequest Message request
     */
    public void processSystemReportRequest(SystemReportRequest message) {
        List<FileMetadata> fileMetadataCopy = new ArrayList<>();
        for (FileMetadata fileMetadata: getController().getFilesMetadata().values()) {
            fileMetadataCopy.add(fileMetadata.copy());
        }

        SystemReportResponse response = new SystemReportResponse(Host.getHostname(), Host.getIpAddress(),
                Constants.CONTROLLER_PORT, fileMetadataCopy);
        sendResponse(this.socket, response);
    }
}
