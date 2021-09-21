package controller.dataStructures;

import java.util.Vector;

public class FileMetadata {
    public String absolutePath;
    public Vector<Vector<ChunkServerMetadata>> chunksServers;

    public FileMetadata(String absolutePath) {
        this.absolutePath = absolutePath;
        this.chunksServers = new Vector<>();
    }

    public FileMetadata(String absolutePath, Vector<Vector<ChunkServerMetadata>> chunksServers) {
        this.absolutePath = absolutePath;
        this.chunksServers = chunksServers;
    }

    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (other instanceof String)
            return ((String) other).equals(this.absolutePath);
        if (!(other instanceof FileMetadata))
            return false;
        FileMetadata otherFileMetadata = (FileMetadata) other;
        return this.absolutePath.equals(otherFileMetadata.absolutePath);
    }

}
