package chunkserver;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ChunkMetadata {

    // The version of the chunk
    public Integer version;

    // The sequence number, or index of the chunk within the file
    public Integer sequence;

    // The timestamp the chunk was last updated
    public Timestamp timestamp;

    // The size in bytes of the raw chunk data
    public Integer sizeBytes;

    // The absolute file path of the file to which the chunk belongs
    public String absoluteFilePath;

    public ChunkMetadata(String absoluteFilePath, Integer sequence) {
        this(absoluteFilePath, sequence, 0);
    }

    public ChunkMetadata(String absoluteFilePath, Integer sequence, Integer sizeBytes) {
        this(absoluteFilePath, 1, sequence, Timestamp.from(Instant.now()), sizeBytes);
    }

    public ChunkMetadata(String absoluteFilePath, Integer version, Integer sequence, Timestamp timestamp, Integer sizeBytes) {
        this.version = version;
        this.sequence = sequence;
        this.timestamp = timestamp;
        this.sizeBytes = sizeBytes;
        this.absoluteFilePath = absoluteFilePath;
    }

    public Integer getVersion() {
        return version;
    }

    public Integer getSequence() {
        return sequence;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Integer getSizeBytes() {
        return sizeBytes;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkMetadata)) return false;
        ChunkMetadata cmOther = (ChunkMetadata) other;
        return (this.version.equals(cmOther.getVersion()) &&
                this.sequence.equals(cmOther.getSequence()) &&
                this.timestamp.getTime() == cmOther.getTimestamp().getTime() &&
                this.sizeBytes.equals(cmOther.getSizeBytes()) &&
                this.absoluteFilePath.equals(cmOther.getAbsoluteFilePath())
        );
    }

    @Override
    public String toString() {
        return "Chunk Metadata:\n" +
                String.format("\tVersion: %d\n", this.version) +
                String.format("\tSequence: %d\n", this.sequence) +
                String.format("\tTimestamp: %s\n", this.timestamp) +
                String.format("\tSize: %s\n", this.sizeBytes) +
                String.format("\tFile: %s\n", this.absoluteFilePath);
    }
}
