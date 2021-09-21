package controller;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chunkserver.ChunkMetadata;
import messaging.ClientWriteRequest;
import messaging.Heartbeat;
import messaging.HeartbeatMajor;
import messaging.HeartbeatMinor;

//Controllers "data structures"
import controller.dataStructures.ChunkServerMetadata;
import controller.dataStructures.FileMetadata;

public class Controller {

    public static Logger log = LoggerFactory.getLogger(Controller.class);

    private Vector<ChunkServerMetadata> chunkServerMetadata = null;
    private Vector<FileMetadata> filesMetadata = null;

    // -- Constructor --

    public Controller() {
        this.chunkServerMetadata = new Vector<ChunkServerMetadata>();
        this.filesMetadata = new Vector<FileMetadata>();
        startServer();

    }

    public void startServer() {
        new ControllerServer(this).launchAsThread();
    }

    // -- Getters --

    /**
     * This method returns the metadata of all the chunk servers.
     * 
     * @return Vector<ChunkServerMetadata>
     */
    public Vector<ChunkServerMetadata> getChunkServerMetadata() {
        return chunkServerMetadata;
    }

    /**
     * This method returns the metadata of all the files.
     * 
     * @return Vector<FileMetadata>
     */
    public Vector<FileMetadata> getFilesMetadata() {
        return filesMetadata;
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

        ChunkServerMetadata chunkServerMetadataToCheck = new ChunkServerMetadata(heartbeat.getHostname(),
                freeSpaceAvailable, totalChunksMaintained, chunkMetadata);

        addOrUpdateChunkServerMetadata(chunkServerMetadataToCheck);
        addOrUpdateFilesMetadata(chunkMetadata, chunkServerMetadataToCheck);
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

        ChunkServerMetadata chunkServerMetadataToCheck = new ChunkServerMetadata(heartbeat.getHostname(),
                freeSpaceAvailable, totalChunksMaintained, null);

        addOrUpdateChunkServerMetadata(chunkServerMetadataToCheck);
    }

    /**
     * This method adds or updates a chunk server in the metadata.
     * @param chunk
     * @synchronized
     */
    public synchronized void addOrUpdateChunkServerMetadata(ChunkServerMetadata chunk) {
        if (!chunkServerMetadata.contains(chunk)) {
            chunkServerMetadata.add(chunk);
            log.info("Added chunk server: {}", chunk.hostname);
        } else {
            log.info("chunk server: {} already being tracked", chunk.hostname);
            updateExistingChunkServerMetadata(chunk);
        }
    }

    /**
     * This method updates the free space available, total chunks maintained and
     * metadata for a specific chunk server.
     * 
     * @synchronized
     * @param chunkServerMetadata
     */
    public synchronized void updateExistingChunkServerMetadata(ChunkServerMetadata chunk) {
        for (ChunkServerMetadata chunkServerMetadata : chunkServerMetadata) {
            if (chunkServerMetadata.hostname.equals(chunk.hostname)) {
                chunkServerMetadata.freeSpaceAvailable = chunk.freeSpaceAvailable;
                if (chunk.chunkMetadata != null) {
                    chunkServerMetadata.chunkMetadata = chunk.chunkMetadata;
                }
                chunkServerMetadata.totalChunksMaintained = chunk.totalChunksMaintained;
                log.info("Updated chunk server: {}", chunkServerMetadata.hostname);
            }
        }
    }

    /**
     * This method adds or updates the metadata of a file.
     * @param chunkMetadata
     * @param heartbeatCSM
     */
    public synchronized void addOrUpdateFilesMetadata(Vector<ChunkMetadata> chunkMetadata,
            ChunkServerMetadata heartbeatCSM) {
        for (ChunkMetadata currentChunkMetadata : chunkMetadata) {
            if (!filesMetadata.contains(currentChunkMetadata.getAbsoluteFilePath())) {
                addNewFileMetadata(currentChunkMetadata, heartbeatCSM);
                log.info("Added file: {}", currentChunkMetadata.getAbsoluteFilePath());
            } else {
                log.info("file: {} already being tracked", currentChunkMetadata.getAbsoluteFilePath());
            }
        }

    }

    /**
     * This is a helper method to addOrUpdateFilesMetaData. This method will add
     * blank vectors to a files chunksSever if the incoming sequence number is not
     * in order. Then it adds it to the chunksServer at that sequence.
     * @param chunkMetadata
     * @param heartbeatCSM
     */
    public synchronized void addNewFileMetadata(ChunkMetadata chunkMetadata, ChunkServerMetadata heartbeatCSM) {
        FileMetadata newFileMetadata = new FileMetadata(chunkMetadata.getAbsoluteFilePath());
        filesMetadata.add(newFileMetadata);
        Integer sequence = chunkMetadata.getSequence();

        if (sequence > newFileMetadata.chunksServers.size()) {
            for (int i = newFileMetadata.chunksServers.size(); i < sequence; i++) {
                newFileMetadata.chunksServers.add(new Vector<>());
            }
        }

        newFileMetadata.chunksServers.get(sequence).add(heartbeatCSM);
    }

}
