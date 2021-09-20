package controller;

import java.util.*;

import chunkserver.ChunkMetadata;

public class Controller {

    // class to act like structs for Controller
    class ChunkServerMetadata {
        public String hostname;
        public Integer port;
        public Integer freeSpaceBytes;
        public Vector<ChunkMetadata> chunkMetadata;
    }

    class FileMetadata {
        public String absolutePath;
        public Vector<Vector<ChunkServerMetadata>> chunksServers; //vector is synchronized
    }
    
    private Vector<ChunkServerMetadata> chunkServerMetadata = null;
    private Vector<FileMetadata> filesMetadata = null;

    // -- Constructor --

    public Controller() {
        init();
        startServer();
    }

    /**
     * This method is called by the constructor upon initialization. It creates
     * the necessary data structures to store the metadata of the chunk servers
     * and the files. It also starts the thread that listens for incoming
     * connections from the chunk servers.
     * @synchronized
     * @return void
     */

    public synchronized void init() {
        //gather all information about chunk servers and files
        this.chunkServerMetadata = new Vector<ChunkServerMetadata>();
        this.filesMetadata = new Vector<FileMetadata>();
        getAllChunkServerMetadata();
        getAllFileMetadata();
    }

    public void startServer() {
        new ControllerServer(this).launchAsThread();
    }

    // -- Getters --

    /**
     * This method returns the metadata of all the chunk servers.
     * @return Vector<ChunkServerMetadata>
     */
    public Vector<ChunkServerMetadata> getChunkServerMetadata() {
        return chunkServerMetadata;
    }

    /**
     * This method returns the metadata of all the files.
     * @return Vector<FileMetadata>
     */
    public Vector<FileMetadata> getFilesMetadata() {
        return filesMetadata;
    }

    // -- Methods For Adding and Removing --

    /**
     * This method adds a new initialized chunk server to the metadata.
     * @param chunkServerMetadata
     * @synchronized
     */
    public synchronized void addChunkServer(ChunkServerMetadata chunkServerMetadata) {
        this.chunkServerMetadata.add(chunkServerMetadata);
    }

    /**
     * This method adds a new file to the metadata.
     * @param fileMetadata
     */
    public synchronized void addFile(FileMetadata fileMetadata) {
        this.filesMetadata.add(fileMetadata);
    }

    /**
     * This method adds a new file metadata. Specifically this method is 
     * used when the file is not yet initialized.
     * @param Vector<Vector<ChunkServerMetadata>>
     * @param absolutePath
     * @synchronized
     */
    public synchronized void addFile(String absolutePath, Vector<Vector<ChunkServerMetadata>> chunksServers) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.absolutePath = absolutePath;
        fileMetadata.chunksServers = chunksServers;
        addFile(fileMetadata);
    }

    /**
     * This method removes a chunk server from the metadata.
     * @param chunkServerMetadata
     * @synchronized
     */
    public synchronized void removeChunkServer(ChunkServerMetadata chunkServerMetadata) {
        this.chunkServerMetadata.remove(chunkServerMetadata);
    }

    /**
     * This method removes a file from the metadata.
     * @param fileMetadata
     * @synchronized
     */
    public synchronized void removeFile(FileMetadata fileMetadata) {
        this.filesMetadata.remove(fileMetadata);
    }
    
    // -- Specific Methods --

    /**
     * This method returns the metadata of all the chunk servers.
     * @returns ChunkServerMetadata
     * @synchronized
     */
    public synchronized ChunkServerMetadata getAllChunkServerMetadata() {
        //TODO: implement
        return null;
    }

    /**
     * This method returns the metadata of all the files.
     * @return FileMetadata
     * @synchronized
     */
    public synchronized FileMetadata getAllFileMetadata() {
        //TODO: implement
        return null;
    }
    
    /**
     * This method updates the free space available for a specific 
     * chunk server.
     * @synchronized
     */
    public synchronized void updateFreeSpaceAvailable(ChunkServerMetadata chunk) {
        //TODO: implement
        // for the passed in chunk, update its free space available
    }

    public synchronized void failureCorrection(ChunkServerMetadata chunk) {
        //TODO: implement
    }

}
