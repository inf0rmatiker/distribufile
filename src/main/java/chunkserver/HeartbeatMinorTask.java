package chunkserver;

import controller.ControllerProcessor;
import messaging.HeartbeatMinor;
import messaging.Message;
import networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
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
            HeartbeatMinor message = constructHeartbeatMinorMessage();
            Client client = new ChunkServerClient(getChunkServer());

            try {
                Socket clientSocket = client.sendMessage(
                        getChunkServer().getControllerHostname(),
                        getChunkServer().getControllerPort(),
                        message
                );
                clientSocket.close();
            } catch (IOException e) {
                log.error("Caught IOException while trying to send HeartbeatMinor Message!");
            }
        } else {
            log.info("Skipping iteration {} of HeartbeatMinorTask; conflicts with HeartbeatMajorTask", this.iteration);
        }
        this.iteration++;
    }

    public HeartbeatMinor constructHeartbeatMinorMessage() {
        // TODO: Construct and return HeartbeatMinor Message
        return null;
    }
}
