package distribufile.chunkserver;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChunkMetadata {

    // The absolute file path of the file to which the chunk belongs
    public String absoluteFilePath;

    // The version of the chunk
    public Integer version;

    // The sequence number, or index of the chunk within the file
    public Integer sequence;

    // The timestamp the chunk was last updated
    public Timestamp timestamp;

    // A list of SHA-1 checksums, one for each slice of the chunk
    public List<String> sliceChecksums;

    public ChunkMetadata(String absoluteFilePath, Integer version, Integer sequence, Timestamp timestamp) {
        this.absoluteFilePath = absoluteFilePath;
        this.version = version;
        this.sequence = sequence;
        this.timestamp = timestamp;
        this.sliceChecksums = new ArrayList<>();
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
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

    public List<String> getSliceChecksums() {
        return sliceChecksums;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkMetadata)) return false;
        ChunkMetadata cmOther = (ChunkMetadata) other;
        return (this.absoluteFilePath.equals(cmOther.getAbsoluteFilePath()) &&
                this.version.equals(cmOther.getVersion()) &&
                this.sequence.equals(cmOther.getSequence()) &&
                this.timestamp.getTime() == cmOther.getTimestamp().getTime());
    }

}
