package controller.dataStructures;

import java.util.Vector;

import chunkserver.ChunkMetadata;

public class ChunkServerMetadata {

    public String hostname;
    public Integer port;
    public Long freeSpaceAvailable;
    public Integer totalChunksMaintained;
    public Vector<ChunkMetadata> chunkMetadata;

    public ChunkServerMetadata(String hostname, Integer port, Long freeSpaceBytes, Integer totalChunksMaintained,
            Vector<ChunkMetadata> chunkMetadata) {
        this.hostname = hostname;
        this.port = port;
        this.freeSpaceAvailable = freeSpaceBytes;
        this.totalChunksMaintained = totalChunksMaintained;
        this.chunkMetadata = chunkMetadata;
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof ChunkServerMetadata))
            return false;
        ChunkServerMetadata otherChunkServerMetadata = (ChunkServerMetadata) other;
        return (this.hostname.equals(otherChunkServerMetadata.hostname)
                && this.port.equals(otherChunkServerMetadata.port));
    }
}
