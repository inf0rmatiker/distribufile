package controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chunkserver.ChunkMetadata;
import messaging.HeartbeatMajor;
import messaging.HeartbeatMinor;

public class Controller {

    public static Logger log = LoggerFactory.getLogger(Controller.class);

    // CSM stands for Chunk Server Metadata
    // FM stands for File Metadata
    private ConcurrentHashMap<String, ChunkServerMetadata> controllerTrackedCSMs = null;
    private ConcurrentHashMap<String, FileMetadata> controllerTrackedFMs = null;

    // -- Constructor --

    public Controller() {
        this.controllerTrackedCSMs = new ConcurrentHashMap<>();
        this.controllerTrackedFMs = new ConcurrentHashMap<>();
    }

    public void startServer() {
        new ControllerServer(this).launchAsThread();
    }

    // -- Getters --

    /**
     * This method returns the metadata of all the chunk servers.
     * 
     * @return ConcurrentHashMap<String, ChunkServerMetadata>
     */
    public ConcurrentHashMap<String, ChunkServerMetadata> getChunkServerMetadata() {
        return controllerTrackedCSMs;
    }

    /**
     * This method returns the metadata of all the files.
     * 
     * @return ConcurrentHashMap<String, FileMetadata>
     */
    public ConcurrentHashMap<String, FileMetadata> getFilesMetadata() {
        return controllerTrackedFMs;
    }

    // -- Specific Methods For Heartbeat Messages --
    // Adds and updates the metadata for chunk servers and files.

    /**
     * this methods uses the heartbeat message to update the metadata of a chunk
     * server, or updates an existing chunk server. This contains all of the data
     * has all of the chunk server data.
     * 
     * @param heartbeat
     * @synchronized
     */
    public synchronized void processHeartbeatMajor(HeartbeatMajor heartbeat) {
        Long freeSpaceAvailable = heartbeat.getFreeSpaceAvailable();
        Integer totalChunksMaintained = heartbeat.getTotalChunksMaintained();
        Vector<ChunkMetadata> chunkMetadata = new Vector<>(heartbeat.getChunksMetadata());

        ChunkServerMetadata csmToCheck = new ChunkServerMetadata(heartbeat.getHostname(), freeSpaceAvailable,
                totalChunksMaintained, chunkMetadata);

        addOrUpdateChunkServerMetadata(csmToCheck);
        addOrUpdateFilesMetadata(chunkMetadata, csmToCheck);
    }

    /**
     * this methods uses the heartbeat message to update the metadata of a chunk
     * server, or updates an existing chunk server. This contains only the hostname,
     * port, freeSpaceAvailable, and totalChunksMaintained.
     * 
     * @param heartbeat
     * @synchronized
     */
    public synchronized void processHeartbeatMinor(HeartbeatMinor heartbeat) {
        Long freeSpaceAvailable = heartbeat.getFreeSpaceAvailable();
        Integer totalChunksMaintained = heartbeat.getTotalChunksMaintained();

        ChunkServerMetadata csmToCheck = new ChunkServerMetadata(heartbeat.getHostname(), freeSpaceAvailable,
                totalChunksMaintained, null);

        addOrUpdateChunkServerMetadata(csmToCheck);
    }

    /**
     * This method adds or updates a chunk server in the metadata.
     * 
     * @param csmToCheck
     * @synchronized
     */
    public synchronized void addOrUpdateChunkServerMetadata(ChunkServerMetadata csmToCheck) {
        if (!controllerTrackedCSMs.containsKey(csmToCheck.hostname)) {
            controllerTrackedCSMs.put(csmToCheck.hostname, csmToCheck);
            log.info("Added chunk server: {}", csmToCheck.hostname);
        } else {
            log.info("chunk server: {} already being tracked", csmToCheck.hostname);
            updateExistingChunkServerMetadata(csmToCheck);
        }
    }

    /**
     * This method updates the free space available, total chunks maintained and
     * metadata for a specific chunk server.
     * 
     * @synchronized
     * @param upToDateCSM
     */
    public synchronized void updateExistingChunkServerMetadata(ChunkServerMetadata upToDateCSM) {
        ChunkServerMetadata csmToUpdate = controllerTrackedCSMs.get(upToDateCSM.hostname);

        csmToUpdate.freeSpaceAvailable = upToDateCSM.freeSpaceAvailable;
        csmToUpdate.totalChunksMaintained = upToDateCSM.totalChunksMaintained;
        if (upToDateCSM.chunkMetadata != null) {
            csmToUpdate.chunkMetadata = upToDateCSM.chunkMetadata;
        }

        log.info("Updated chunk server: {}", csmToUpdate.hostname);
    }

    /**
     * This method adds or updates the metadata of a file.
     * 
     * @param csmToCheck
     * @param heartbeatCSM
     */
    public synchronized void addOrUpdateFilesMetadata(Vector<ChunkMetadata> csmToCheck,
            ChunkServerMetadata heartbeatCSM) {

        for (ChunkMetadata currChunkMetadata : csmToCheck) {
            String absolutePathToCheck = currChunkMetadata.getAbsoluteFilePath();

            if (!controllerTrackedFMs.containsKey(absolutePathToCheck)) {
                addNewFileMetadata(currChunkMetadata, heartbeatCSM);
                log.info("Added file: {}", absolutePathToCheck);
            } else {
                log.info("file: {} already being tracked", absolutePathToCheck);
            }
        }

    }

    /**
     * This is a helper method to addOrUpdateFilesMetaData. This method will add
     * blank vectors to a files chunksSever if the incoming sequence number is not
     * in order. Then it adds it to the chunksServer at that sequence.
     * 
     * @param chunkMetadata
     * @param heartbeatCSM
     */
    public synchronized void addNewFileMetadata(ChunkMetadata chunkMetadata, ChunkServerMetadata heartbeatCSM) {
        String absolutePath = chunkMetadata.getAbsoluteFilePath();
        FileMetadata newFileMetadata = new FileMetadata(chunkMetadata.getAbsoluteFilePath());
        controllerTrackedFMs.put(absolutePath, newFileMetadata);
        fillFilesChunkServerMetadata(newFileMetadata, chunkMetadata, heartbeatCSM);
    }

    /**
     * This method fills a new file metadata with the chunk server metadata.
     * 
     * @param newFileMetadata
     * @param chunkMetadata
     * @param heartbeatCSM
     */
    public synchronized void fillFilesChunkServerMetadata(FileMetadata newFileMetadata, ChunkMetadata chunkMetadata,
            ChunkServerMetadata heartbeatCSM) {
        Integer sequence = chunkMetadata.getSequence();

        if (sequence > newFileMetadata.chunksServers.size()) {
            for (int i = newFileMetadata.chunksServers.size(); i <= sequence; i++) {
                newFileMetadata.chunksServers.add(new Vector<>());
            }
        }

        newFileMetadata.chunksServers.get(sequence).add(heartbeatCSM);
    }

}
