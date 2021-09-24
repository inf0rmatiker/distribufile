package controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chunkserver.ChunkMetadata;

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

    public void startServer() {
        new ControllerServer(this).launchAsThread();
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
     * Updates the free space available, total chunks maintained and
     * metadata for a specific chunk server.
     * @param csm ChunkServerMetadata we received for updating the original
     */
    public synchronized void updateChunkServerMetadata(ChunkServerMetadata csm) {
        if (this.trackedChunkServerMetadata.containsKey(csm.hostname)) {
            ChunkServerMetadata old = this.trackedChunkServerMetadata.get(csm.hostname);
            old.freeSpaceAvailable = csm.freeSpaceAvailable;
            old.totalChunksMaintained = csm.totalChunksMaintained;
            old.chunkMetadata.addAll(csm.chunkMetadata);
            log.info("Updated Chunk Server \"{}\" metadata", csm.hostname);
        } else {
            this.trackedChunkServerMetadata.put(csm.hostname, csm);
            log.info("Added Chunk Server metadata: {}", csm);
        }
    }

    /**
     * Replaces the Chunk Server metadata information for a given Chunk Server,
     * based on information from a Major heartbeat.
     * @param csm ChunkServerMetadata we received for replacing the original
     */
    public synchronized void replaceChunkServerMetadata(ChunkServerMetadata csm) {
        ChunkServerMetadata prev = this.trackedChunkServerMetadata.put(csm.hostname, csm);
        if (prev == null) {
            log.info("Added Chunk Server metadata: {}", csm);
        } else {
            log.info("Replaced Chunk Server \"{}\" metadata", csm.hostname);
        }
    }

    /**
     * Updates information we know about the files with chunk metadata reported by a single Chunk Server.
     * For each chunk's metadata object, we update the corresponding file metadata to have the Chunk Server
     * hostname held at that chunk's sequence index.
     * @param chunksMetadata ChunkMetadata for all the chunks reported by a Chunk Server in a Major Heartbeat
     * @param chunkServerHostname The hostname of the Chunk Server holding the aforementioned chunks
     */
    public synchronized void updateFilesMetadata(Vector<ChunkMetadata> chunksMetadata, String chunkServerHostname) {
        for (ChunkMetadata chunkMetadata : chunksMetadata) {
            String filename = chunkMetadata.getAbsoluteFilePath();
            FileMetadata fileMetadata = this.trackedFileMetadata.getOrDefault(filename, new FileMetadata(filename));
            fileMetadata.put(chunkServerHostname, chunkMetadata.getSequence());
        }
    }

}
