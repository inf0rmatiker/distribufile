package chunkserver;

import messaging.HeartbeatMajor;
import messaging.HeartbeatMinor;
import networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
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
        HeartbeatMajor message = constructHeartbeatMajorMessage();

        try {
            Socket clientSocket = Client.sendMessage(
                    getChunkServer().getControllerHostname(),
                    getChunkServer().getControllerPort(),
                    message
            );

            clientSocket.close();
        } catch (IOException e) {
            log.error("Caught IOException while trying to send HeartbeatMajor Message!");
        }
        this.iteration++;
    }

    public HeartbeatMajor constructHeartbeatMajorMessage() {
        // TODO: Construct and return HeartbeatMajor Message
        return null;
    }
}
