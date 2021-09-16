package chunkserver;

import util.Constants;

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkServer {

    public LinkedBlockingQueue<ChunkMetadata> newlyAddedChunks;
    public String controllerHostname;
    public Integer controllerPort;

    public ChunkServer(String controllerHostname, Integer controllerPort) {
        this.controllerHostname = controllerHostname;
        this.controllerPort = controllerPort;
        this.newlyAddedChunks = new LinkedBlockingQueue<>();
    }

    public LinkedBlockingQueue<ChunkMetadata> getNewlyAddedChunks() {
        return newlyAddedChunks;
    }

    public String getControllerHostname() {
        return controllerHostname;
    }

    public Integer getControllerPort() {
        return controllerPort;
    }

    public void discoverChunks() {
        // TODO: Traverse chunkDir and discover all chunk files
    }

    public void discoverFreeSpaceAvailable() {
        // TODO: Use system command (df -Th | grep /tmp) to discover available space
    }

    /**
     * Starts the server as a thread for accepting incoming connections via Sockets
     */
    public void startServer() {
        new ChunkServerServer(this).launchAsThread();
    }

    /**
     * Starts the timer-based thread for sending HeartbeatMinor messages at regular intervals
     */
    public void startHeartbeatMinorTask() {
        Timer heartbeatMinorDaemon = new Timer("HeartbeatMinorTask", true);
        heartbeatMinorDaemon.schedule(new HeartbeatMinorTask(this),
                0, Constants.HEARTBEAT_MINOR_INTERVAL);
    }

    /**
     * Starts the timer-based thread or sending HeartbeatMajor messages at regular intervals
     */
    public void startHeartbeatMajorTask() {
        Timer heartbeatMajorDaemon = new Timer("HeartbeatMajorTask", true);
        heartbeatMajorDaemon.schedule(new HeartbeatMajorTask(this),
                Constants.HEARTBEAT_MAJOR_INTERVAL, Constants.HEARTBEAT_MAJOR_INTERVAL);
    }

}
