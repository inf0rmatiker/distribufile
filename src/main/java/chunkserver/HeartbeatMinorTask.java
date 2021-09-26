package chunkserver;

import controller.ControllerProcessor;
import messaging.HeartbeatMinor;
import messaging.Message;
import networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class HeartbeatMinorTask extends TimerTask {

    public static Logger log = LoggerFactory.getLogger(HeartbeatMinorTask.class);

    public long iteration = 0;
    public ChunkServer chunkServer;

    public HeartbeatMinorTask(ChunkServer chunkServer) {
        this.chunkServer = chunkServer;
    }

    public ChunkServer getChunkServer() {
        return chunkServer;
    }

    @Override
    public void run() {
        if (this.iteration % 10 != 0) {
            log.info("Iteration {} of HeartbeatMinorTask", this.iteration);

            try {
                HeartbeatMinor message = constructHeartbeatMinorMessage();

                try {
                    Socket clientSocket = Client.sendMessage(
                            getChunkServer().getControllerHostname(),
                            getChunkServer().getControllerPort(),
                            message
                    );
                    clientSocket.close();
                } catch (IOException e) {
                    log.error("Caught IOException while trying to send HeartbeatMinor Message: {}", e.getMessage());
                }

            } catch (IOException e) {
                log.error("Unable to create HeartbeatMinor Message: {}", e.getMessage());
            }

        } else {
            log.info("Skipping iteration {} of HeartbeatMinorTask; conflicts with HeartbeatMajorTask", this.iteration);
        }
        this.iteration++;
    }

    /**
     * Creates a new HeartbeatMinor Message with up-to-date information about the free space available,
     * total chunks maintained by the Chunk Server, and metadata about newly added chunks.
     * @return HeartbeatMinor Message
     * @throws IOException If unable to discover chunk files
     */
    public HeartbeatMinor constructHeartbeatMinorMessage() throws IOException {
        long freeSpaceAvailable = getChunkServer().discoverFreeSpaceAvailable();
        int totalChunksMaintained = getChunkServer().discoverChunks().size();
        List<ChunkMetadata> newChunks = new ArrayList<>();
        getChunkServer().getNewlyAddedChunks().drainTo(newChunks); // consume all newly added chunks

        return new HeartbeatMinor(Host.getHostname(), Host.getIpAddress(), Constants.CHUNK_SERVER_PORT,
                totalChunksMaintained, freeSpaceAvailable, newChunks, new ArrayList<>());
    }
}
