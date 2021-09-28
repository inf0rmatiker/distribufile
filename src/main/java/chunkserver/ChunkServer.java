package chunkserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
     */
    public synchronized List<String> discoverChunks() {
        List<String> chunkFilenames = new ArrayList<>();

        // Apparently this is how you have to implement a walker that skips directories it can't read
        try {
            Files.walkFileTree(Paths.get(Chunk.getChunkDir()), new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        String filename = file.toString();
                        if (filename.contains("_chunk")) {
                            chunkFilenames.add(file.toString());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (attrs.isDirectory() && !Files.isReadable(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) {
                    if (e == null) {
                        return FileVisitResult.CONTINUE;
                    } else {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }

                @Override
                public FileVisitResult visitFileFailed(Path dir, IOException e) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });

        } catch (IOException e) {
            log.error("Caught IOException while walking directory tree! {}", e.getMessage());
            e.printStackTrace();
        }
        return chunkFilenames;
    }

    /**
     * Reads all the stored chunk files for their metadata, returning as a list
     * @return List of ChunkMetadata objects, one for each stored chunk file
     */
    public synchronized List<ChunkMetadata> discoverChunksMetadata() {
        List<ChunkMetadata> chunkMetadataList = new ArrayList<>();
        List<String> chunkFiles = discoverChunks();
        for (String filename: chunkFiles) {
            ChunkFilename chunkFilename = new ChunkFilename(filename, Chunk.getChunkDir());
            chunkMetadataList.add(new ChunkMetadata(chunkFilename.getClientAbsolutePath(), chunkFilename.getChunkSequence()));
        }
        return chunkMetadataList;
    }

    /**
     * Gets the free space, in bytes, available at the chunk storage directory.
     * @return Long free bytes available
     */
    public synchronized Long discoverFreeSpaceAvailable() {
        return new File(Chunk.getChunkDir()).getFreeSpace();
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
     * Starts the timer-based thread for sending HeartbeatMajor messages at regular intervals
     */
    public void startHeartbeatMajorTask() {
        log.info("Starting Major Heartbeat Task...");
        Timer heartbeatMajorDaemon = new Timer("HeartbeatMajorTask", true);
        heartbeatMajorDaemon.schedule(new HeartbeatMajorTask(this),
                0, Constants.HEARTBEAT_MAJOR_INTERVAL);
    }

}
