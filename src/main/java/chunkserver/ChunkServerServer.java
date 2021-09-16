package chunkserver;

import networking.Processor;
import networking.Server;
import util.Constants;

import java.net.Socket;

public class ChunkServerServer extends Server {

    // Reference to the original ChunkServer instance, passed to Processor at connection acceptance time
    public ChunkServer chunkServer;

    public ChunkServerServer(ChunkServer chunkServer) {
        this.chunkServer = chunkServer;
        this.bindToPort(Constants.CHUNK_SERVER_PORT);
    }

    public ChunkServer getChunkServer() {
        return chunkServer;
    }

    @Override
    public void processConnection(Socket clientSocket) {
        Processor processor = new ChunkServerProcessor(clientSocket, getChunkServer());
        processor.launchAsThread();
    }
}
