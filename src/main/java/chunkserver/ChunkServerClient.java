package chunkserver;

import messaging.Message;
import networking.Client;

public class ChunkServerClient extends Client {

    public ChunkServer chunkServer;

    public ChunkServerClient(ChunkServer chunkServer) {
        this.chunkServer = chunkServer;
    }

    public ChunkServer getChunkServer() {
        return chunkServer;
    }



}
