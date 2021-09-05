package chunkserver;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChunkMetadata {

    public String absoluteFilePath;
    public Integer version;
    public Integer sequence;
    public Timestamp timestamp;
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
