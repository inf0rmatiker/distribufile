package chunkserver;

import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class ChunkServer {

    public static Logger log = LoggerFactory.getLogger(ChunkServer.class);

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

    /**
     * Recursively walks the chunk storage directory and returns a list of absolute paths of chunk files
     * @return All chunk files' absolute paths
     * @throws IOException If unable to read
     */
    public List<String> discoverChunks() throws IOException {
        List<String> chunkFilenames = new ArrayList<>();
        Stream<Path> paths = Files.walk(Paths.get(Chunk.getChunkDir()));
        paths.filter(Files::isRegularFile).forEach(
                file -> {
                    String filename = file.toString();
                    if (filename.contains("_chunk")) chunkFilenames.add(filename);
                }
        );
        return chunkFilenames;
    }

    public void discoverFreeSpaceAvailable() {
        // TODO: Use system command (df -Th | grep /tmp) to discover available space
    }

    /**
     * Starts the server as a thread for accepting incoming connections via Sockets
     */
    public void startServer() {
        log.info("Starting Chunk Server...");
        new ChunkServerServer(this).launchAsThread();
    }

    /**
     * Starts the timer-based thread for sending HeartbeatMinor messages at regular intervals
     */
    public void startHeartbeatMinorTask() {
        log.info("Starting Minor Heartbeat Task...");
        Timer heartbeatMinorDaemon = new Timer("HeartbeatMinorTask", true);
        heartbeatMinorDaemon.schedule(new HeartbeatMinorTask(this),
                0, Constants.HEARTBEAT_MINOR_INTERVAL);
    }

    /**
     * Starts the timer-based thread or sending HeartbeatMajor messages at regular intervals
     */
    public void startHeartbeatMajorTask() {
        log.info("Starting Major Heartbeat Task...");
        Timer heartbeatMajorDaemon = new Timer("HeartbeatMajorTask", true);
        heartbeatMajorDaemon.schedule(new HeartbeatMajorTask(this),
                Constants.HEARTBEAT_MAJOR_INTERVAL, Constants.HEARTBEAT_MAJOR_INTERVAL);
    }

}
