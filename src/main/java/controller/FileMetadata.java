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

    /**
     * Puts a Chunk Server hostname in the set of hostnames for a given chunk
     * @param chunkServerHostname The hostname of the Chunk Server maintaining the chunk
     * @param sequence The sequence index of the chunk within the file
     */
    public void put(String chunkServerHostname, int sequence) {
        if (this.chunkServerHostnames.size() <= sequence) {
            fillChunkGaps(sequence);
        }
        Set<String> existingHostnames = this.chunkServerHostnames.get(sequence);
        existingHostnames.add(chunkServerHostname);
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

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof FileMetadata)) return false;
        FileMetadata otherFileMetadata = (FileMetadata) other;
        return this.absolutePath.equals(otherFileMetadata.absolutePath);
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
