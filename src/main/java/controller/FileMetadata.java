package controller;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class FileMetadata {

    // The absolute path of the file, as it appears to the client
    public String absolutePath;

    /* Contains multiple Sets, one per chunk in the file, each of which contains the hostnames
       at which the chunk resides. Example for a file with 4 chunks:
       [
            ( "shark", "tuna", "stingray" ),  // chunk 0
            ( "salmon", "trout", "bass" ),    // chunk 1
            ( "catfish", "pike", "lobster" ), // chunk 2
            ( "whale", "dolphin", "orca" )    // chunk 3
        ]
     */
    public Vector<Set<String>> chunkServerHostnames;

    public FileMetadata(String absolutePath) {
        this.absolutePath = absolutePath;
        this.chunkServerHostnames = new Vector<>();
    }

    public FileMetadata(String absolutePath, Vector<Set<String>> chunkServerHostnames) {
        this.absolutePath = absolutePath;
        this.chunkServerHostnames = chunkServerHostnames;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public Vector<Set<String>> getChunkServerHostnames() {
        return chunkServerHostnames;
    }

    /**
     * Puts a Chunk Server hostname in the set of hostnames for a given chunk
     * @param chunkServerHostname The hostname of the Chunk Server maintaining the chunk
     * @param sequence The sequence index of the chunk within the file
     */
    public synchronized void put(String chunkServerHostname, int sequence) {
        if (this.chunkServerHostnames.size() <= sequence) {
            fillChunkGaps(sequence);
        }
        this.chunkServerHostnames.get(sequence).add(chunkServerHostname);
    }

    /**
     * Sets a chunk's replication servers to the Set provided
     * @param chunkServerHostnames Set of Chunk Server hostnames for a chunk replica
     * @param sequence The sequence index of the chunk within the file
     */
    public synchronized void put(Set<String> chunkServerHostnames, int sequence) {
        if (this.chunkServerHostnames.size() <= sequence) {
            fillChunkGaps(sequence);
        }
        this.chunkServerHostnames.set(sequence, chunkServerHostnames);
    }

    /**
     * Retrieves the set of Chunk Server hostnames which hold a certain chunk
     * @param sequence int sequence of the chunk
     * @return Set of hostnames, or null if sequence is out of bounds
     */
    public synchronized Set<String> get(int sequence) {
        if (sequence < this.chunkServerHostnames.size()) {
            return this.chunkServerHostnames.get(sequence);
        }
        return null;
    }

    /**
     * Fills the Vector gaps with new HashSets up to a given sequence index
     * @param sequence Sequence index of the chunk in the file
     */
    public void fillChunkGaps(int sequence) {
        while (chunkServerHostnames.size() <= sequence) {
            chunkServerHostnames.add(new HashSet<>());
        }
    }

    /**
     * @return a deep-copied FileMetadata object from this
     */
    public synchronized FileMetadata copy() {
        String absoluteFilePathCopy = this.absolutePath;
        Vector<Set<String>> chunkServerHostnamesCopy = new Vector<>();
        for (Set<String> chunkHosts: this.chunkServerHostnames) {
            chunkServerHostnamesCopy.add(new HashSet<>(chunkHosts));
        }
        return new FileMetadata(absoluteFilePathCopy, chunkServerHostnamesCopy);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof FileMetadata)) return false;
        FileMetadata otherFileMetadata = (FileMetadata) other;
        return (this.absolutePath.equals(otherFileMetadata.absolutePath) &&
                this.chunkServerHostnames.equals(otherFileMetadata.getChunkServerHostnames()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FileMetadata:\n");
        sb.append(String.format("  absolutePath: %s\n", this.absolutePath));
        if (this.chunkServerHostnames.isEmpty()) {
            sb.append("  chunkServerHostnames: [ ]\n");
        } else {
            sb.append("  chunkServerHostnames: [\n");
            for (Set<String> hostnames: this.chunkServerHostnames) {
                sb.append("\t");
                sb.append(hostnames);
                sb.append("\n");
            }
            sb.append("  ]\n");
        }

        return sb.toString();
    }
}
