package controller.dataStructures;

import java.util.Vector;

public class FileMetadata {
    public String absolutePath;
    public Vector<Vector<ChunkServerMetadata>> chunksServers; // vector is synchronized
}
