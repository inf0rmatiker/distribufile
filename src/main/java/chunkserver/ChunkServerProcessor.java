package chunkserver;

import controller.ControllerProcessor;
import messaging.ChunkStoreRequest;
import messaging.Message;
import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.info("Processing Message for ChunkServerProcessor");

        switch(message.getType()) {
            case CHUNK_STORE_REQUEST:
                processChunkStoreRequest((ChunkStoreRequest) message);
                break;
            default: log.error("Unimplemented Message type \"{}\"", message.getType());
        }
    }

    public void processChunkStoreRequest(ChunkStoreRequest message) {
        // TODO: Process ChunkStoreRequest Message
        log.info("ChunkServerProcessor: processing ChunkStoreRequest: TODO");
    }

}
