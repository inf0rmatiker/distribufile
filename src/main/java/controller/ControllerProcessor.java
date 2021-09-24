package controller;

import chunkserver.ChunkMetadata;
import messaging.HeartbeatMajor;
import messaging.HeartbeatMinor;
import messaging.Message;
import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
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
                // TODO: Implement
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
}
