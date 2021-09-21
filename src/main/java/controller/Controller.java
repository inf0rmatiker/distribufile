package controller;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chunkserver.ChunkMetadata;
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

    // -- Methods For Adding and Removing --

    /**
     * This method adds a new initialized chunk server to the metadata.
     * 
     * @param chunkServerMetadata
     * @synchronized
     */
    public synchronized void addChunkServer(ChunkServerMetadata chunkServerMetadata) {
        this.chunkServerMetadata.add(chunkServerMetadata);
    }

    /**
     * This method adds a new file to the metadata.
     * 
     * @param fileMetadata
     */
    public synchronized void addFile(FileMetadata fileMetadata) {
        this.filesMetadata.add(fileMetadata);
    }

    /**
     * This method removes a chunk server from the metadata.
     * 
     * @param chunkServerMetadata
     * @synchronized
     */
    public synchronized void removeChunkServerMetadata(ChunkServerMetadata chunkServerMetadata) {
        this.chunkServerMetadata.remove(chunkServerMetadata);
    }

    /**
     * This method removes a file from the metadata.
     * 
     * @param fileMetadata
     * @synchronized
     */
    public synchronized void removeFileMetadata(FileMetadata fileMetadata) {
        this.filesMetadata.remove(fileMetadata);
    }

    // -- Specific Methods --

    public synchronized void processHeartbeatMajor(HeartbeatMajor heartbeat) {

        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(heartbeat.getMarshaledBytes());
                DataInputStream dataInputStream = new DataInputStream(byteInputStream);) {
            dataInputStream.readInt(); // skip byte
        } catch (IOException e) {
            log.error("Error reading heartbeat major", e);
        }

        Long freeSpaceAvailable = heartbeat.getFreeSpaceAvailable();
        Integer totalChunksMaintained = heartbeat.getTotalChunksMaintained();
        Vector<ChunkMetadata> chunkMetadata = new Vector<>(heartbeat.getChunksMetadata());

        ChunkServerMetadata chunkServerMetadataToCheck = new ChunkServerMetadata(heartbeat.getHostname(),
                heartbeat.getPort(), freeSpaceAvailable, totalChunksMaintained, chunkMetadata);

        addOrUpdateChunkServerMetadata(chunkServerMetadataToCheck);
    }

    public synchronized void processHeartbeatMinor(HeartbeatMinor heartbeat) {

        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(heartbeat.getMarshaledBytes());
                DataInputStream dataInputStream = new DataInputStream(byteInputStream);) {
            dataInputStream.readInt(); // skip byte
        } catch (IOException e) {
            log.error("Error reading heartbeat minor", e);
        }

        Long freeSpaceAvailable = heartbeat.getFreeSpaceAvailable();
        Integer totalChunksMaintained = heartbeat.getTotalChunksMaintained(); 
        
        ChunkServerMetadata chunkServerMetadataToCheck = new ChunkServerMetadata(heartbeat.getHostname(),
                heartbeat.getPort(), freeSpaceAvailable, totalChunksMaintained, null);
        
        addOrUpdateChunkServerMetadata(chunkServerMetadataToCheck);
    }

    public synchronized void addOrUpdateChunkServerMetadata(ChunkServerMetadata chunk) {
        if (!chunkServerMetadata.contains(chunk)) {
            chunkServerMetadata.add(chunk);
            log.info("Added chunk server: {}:{}", chunk.hostname, chunk.port);
        } else {
            log.info("chunk server already being tracked");
            updateExistingChunkServerMetadata(chunk);
        }
    }

    /**
     * This method updates the free space available and metadata for a specific
     * chunk server.
     * 
     * @synchronized
     * @param chunkServerMetadata
     */
    public synchronized void updateExistingChunkServerMetadata(ChunkServerMetadata chunk) {
        for (ChunkServerMetadata chunkServerMetadata : chunkServerMetadata) {
            if (chunkServerMetadata.hostname.equals(chunk.hostname) && chunkServerMetadata.port == chunk.port) {
                chunkServerMetadata.freeSpaceAvailable = chunk.freeSpaceAvailable;
                if (chunk.chunkMetadata != null) {
                    chunkServerMetadata.chunkMetadata = chunk.chunkMetadata;
                }
                chunkServerMetadata.totalChunksMaintained = chunk.totalChunksMaintained;
                log.info("Updated chunk server: {}:{}", chunkServerMetadata.hostname, chunkServerMetadata.port);
            }
        }
    }

}
