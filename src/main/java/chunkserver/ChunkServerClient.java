package chunkserver;

import messaging.Message;
import networking.Client;

public class ChunkServerClient extends Client implements Runnable {

    public ChunkServer chunkServer;

    public ChunkServerClient(ChunkServer chunkServer) {
        this.chunkServer = chunkServer;
    }

    public ChunkServer getChunkServer() {
        return chunkServer;
    }

    @Override
    public void run() {
        // TODO: Wait for response, then process it
    }

    @Override
    public void processResponse(Message message) {
        // TODO: Process Message message
    }
}
