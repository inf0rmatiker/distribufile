package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * Responsible for monitoring the heartbeats of Chunk Servers.
 * If a Chunk Server fails to send a heartbeat at its expected interval (plus some grace period),
 * it is considered dead handled accordingly.
 */
public class HeartbeatMonitor extends TimerTask {

    public static Logger log = LoggerFactory.getLogger(HeartbeatMonitor.class);

    public Controller controller;

    public HeartbeatMonitor(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public void run() {
        for (ChunkServerMetadata chunkServer: getController().getChunkServerMetadata().values()) {
            if (chunkServer.isExpired()) {
                log.warn("Chunk Server {} has failed to send any heartbeat messages within interval, expiring it",
                        chunkServer.getHostname());
            }
        }
    }
}
