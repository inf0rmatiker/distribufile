package chunkserver;

import messaging.HeartbeatMajor;
import messaging.HeartbeatMinor;
import networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.TimerTask;

public class HeartbeatMajorTask extends TimerTask {

    public static Logger log = LoggerFactory.getLogger(HeartbeatMajorTask.class);

    public long iteration = 0;
    public ChunkServer chunkServer;

    public HeartbeatMajorTask(ChunkServer chunkServer) {
        this.chunkServer = chunkServer;
    }

    public ChunkServer getChunkServer() {
        return chunkServer;
    }

    @Override
    public void run() {
        log.info("Iteration {} of HeartbeatMajorTask", this.iteration);

        try {
            HeartbeatMajor message = constructHeartbeatMajorMessage();

            try {
                Socket clientSocket = Client.sendMessage(
                        getChunkServer().getControllerHostname(),
                        getChunkServer().getControllerPort(),
                        message
                );

                clientSocket.close();
            } catch (IOException e) {
                log.error("Caught IOException while trying to send HeartbeatMajor Message: {}", e.getMessage());
            }

        } catch (IOException e) {
            log.error("Unable to create HeartbeatMajor Message: {}", e.getMessage());
        }

        this.iteration++;
    }

    /**
     * Creates a new HeartbeatMajor Message with up-to-date information about the free space available,
     * total chunks maintained by the Chunk Server, and metadata about all maintained chunks.
     * @return HeartbeatMajor Message
     * @throws IOException If unable to read from disk
     */
    public HeartbeatMajor constructHeartbeatMajorMessage() throws IOException {
        long freeSpaceAvailable = getChunkServer().discoverFreeSpaceAvailable();
        List<ChunkMetadata> chunkMetadataList = getChunkServer().discoverChunksMetadata();
        int totalChunksMaintained = chunkMetadataList.size();

        return new HeartbeatMajor(Host.getHostname(), Host.getIpAddress(), Constants.CHUNK_SERVER_PORT,
                totalChunksMaintained, freeSpaceAvailable, chunkMetadataList);
    }
}
