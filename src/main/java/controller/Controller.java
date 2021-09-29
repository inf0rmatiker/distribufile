package controller;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import chunkserver.HeartbeatMajorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chunkserver.ChunkMetadata;
import util.Constants;

public class Controller {

    public static Logger log = LoggerFactory.getLogger(Controller.class);

    // Maintains metadata about all Chunk Servers tracked by this Controller
    private final ConcurrentHashMap<String, ChunkServerMetadata> trackedChunkServerMetadata;

    // Maintains metadata about all files tracked by this Controller
    private final ConcurrentHashMap<String, FileMetadata> trackedFileMetadata;

    public Controller() {
        this.trackedChunkServerMetadata = new ConcurrentHashMap<>();
        this.trackedFileMetadata = new ConcurrentHashMap<>();
    }

    /**
     * Starts the server as a thread for accepting incoming connections via Sockets
     */
    public void startServer() {
        new ControllerServer(this).launchAsThread();
    }

    /**
     * Starts the timer-based thread for checking liveness of Chunk Servers by their heartbeats
     */
    public void startHeartbeatMonitor() {
        log.info("Starting Heartbeat Monitor...");
        Timer heartbeatMonitorDaemon = new Timer("HeartbeatMonitor", true);
        heartbeatMonitorDaemon.schedule(new HeartbeatMonitor(this), 0, Constants.HEARTBEAT_GRACE_PERIOD);
    }

    /**
     * Returns the metadata of all the chunk servers.
     * @return ConcurrentHashMap<String, ChunkServerMetadata>
     */
    public ConcurrentHashMap<String, ChunkServerMetadata> getChunkServerMetadata() {
        return trackedChunkServerMetadata;
    }

    /**
     * Returns the metadata of all the files.
     * @return ConcurrentHashMap<String, FileMetadata>
     */
    public ConcurrentHashMap<String, FileMetadata> getFilesMetadata() {
        return trackedFileMetadata;
    }


    /**
     * Updates the ChunkServerMetadata for a given Chunk Server
     * @param hostname String hostname of the Chunk Server we are updating
     * @param freeSpaceAvailable long free space available in bytes
     * @param totalChunksMaintained int total number of chunks maintained
     * @param chunkMetadata List of ChunkMetadata objects to be added to the tracked ChunkMetadata Vector
     */
    public synchronized void updateChunkServerMetadata(String hostname, long freeSpaceAvailable,
                                                       int totalChunksMaintained, List<ChunkMetadata> chunkMetadata) {
        if (this.trackedChunkServerMetadata.containsKey(hostname)) {
            ChunkServerMetadata old = this.trackedChunkServerMetadata.get(hostname);
            old.lastRecordedHeartbeat = Timestamp.from(Instant.now()); // update heartbeat timestamp
            old.freeSpaceAvailable = freeSpaceAvailable;
            old.totalChunksMaintained = totalChunksMaintained;
            old.chunkMetadata.addAll(chunkMetadata);
        } else { // not yet tracking this Chunk Server; do so now
            this.trackedChunkServerMetadata.put(hostname, new ChunkServerMetadata(
                    hostname, freeSpaceAvailable, totalChunksMaintained, new Vector<>(chunkMetadata)
            ));
        }
    }


    /**
     * Replaces the ChunkServerMetadata for a given Chunk Server
     * @param hostname String hostname of the Chunk Server we are replacing
     * @param freeSpaceAvailable long free space available in bytes
     * @param totalChunksMaintained int total number of chunks maintained
     * @param chunkMetadata List of ChunkMetadata objects to be added to the tracked ChunkMetadata Vector
     */
    public synchronized void replaceChunkServerMetadata(String hostname, Long freeSpaceAvailable,
                                                        int totalChunksMaintained, List<ChunkMetadata> chunkMetadata) {
        ChunkServerMetadata prev = this.trackedChunkServerMetadata.put(hostname, new ChunkServerMetadata(
                hostname, freeSpaceAvailable, totalChunksMaintained, new Vector<>(chunkMetadata))
        );
        if (prev == null) {
            log.info("Added Chunk Server metadata: {}", this.trackedChunkServerMetadata.get(hostname));
        }
    }

    /**
     * Updates information we know about the files with chunk metadata reported by a single Chunk Server.
     * For each chunk's metadata object, we update the corresponding file metadata to have the Chunk Server
     * hostname held at that chunk's sequence index.
     * @param chunksMetadata ChunkMetadata for all the chunks reported by a Chunk Server in a Major Heartbeat
     * @param chunkServerHostname The hostname of the Chunk Server holding the aforementioned chunks
     */
    public synchronized void updateFilesMetadata(List<ChunkMetadata> chunksMetadata, String chunkServerHostname) {
        for (ChunkMetadata chunkMetadata : chunksMetadata) {
            String filename = chunkMetadata.getAbsoluteFilePath();
            FileMetadata fileMetadata = this.trackedFileMetadata.getOrDefault(filename, new FileMetadata(filename));
            fileMetadata.put(chunkServerHostname, chunkMetadata.getSequence());
        }
    }

    /**
     * Selects the k best Chunk Servers to store a chunk replica on.
     * Uses the criteria of totalChunksMaintained, a lower number being better.
     * Algorithm used is Unordered Partial Sort, and is O(kn): https://en.wikipedia.org/wiki/Selection_algorithm,
     * where k is the number of selections we want, and n is the number of Chunk Servers we know about.
     *
     * @return Set of k best Chunk Server hostnames to store a replica on
     */
    public Set<String> selectBestChunkServersForReplicas() {
        int k = Constants.CHUNK_REPLICATION;
        List<ChunkServerMetadata> chunkServerMetadata = new ArrayList<>(getChunkServerMetadata().values());
        for (int i = 0; i < k; i++) {
            int bestIndex = i;
            ChunkServerMetadata bestValue = chunkServerMetadata.get(i);
            for (int j = i+1; j < chunkServerMetadata.size(); j++) {
                if (chunkServerMetadata.get(j).getTotalChunksMaintained() < bestValue.getTotalChunksMaintained()) {
                    bestIndex = j;
                    bestValue = chunkServerMetadata.get(j);
                    Collections.swap(chunkServerMetadata, i, bestIndex);
                }
            }
        }

        // At this point, the first k elements of chunkServerMetadata are the best ones, unsorted,
        // so we just dump them into a Set and return it
        Set<String> bestSet = new HashSet<>();
        for (int i = 0; i < k; i++) {
            bestSet.add(chunkServerMetadata.get(i).getHostname());
        }
        return bestSet;
    }

}
