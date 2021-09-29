package controller;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Vector;

import chunkserver.ChunkMetadata;
import util.Constants;

public class ChunkServerMetadata {

    // Hostname of the Chunk Server
    public String hostname;

    // Free space, in bytes, available on the Chunk Server
    public Long freeSpaceAvailable;

    // Count of total chunks maintained by the Chunk Server
    public Integer totalChunksMaintained;

    // List of metadata objects for each chunk maintained by the Chunk Server
    public Vector<ChunkMetadata> chunkMetadata;

    // The timestamp of the last received heartbeat from the Chunk Server
    public Timestamp lastRecordedHeartbeat;

    public ChunkServerMetadata(String hostname, Long freeSpaceBytes, Integer totalChunksMaintained,
            Vector<ChunkMetadata> chunkMetadata) {
        this.hostname = hostname;
        this.freeSpaceAvailable = freeSpaceBytes;
        this.totalChunksMaintained = totalChunksMaintained;
        this.chunkMetadata = chunkMetadata;
        this.lastRecordedHeartbeat = Timestamp.from(Instant.now());
    }

    public String getHostname() {
        return hostname;
    }

    public Long getFreeSpaceAvailable() {
        return freeSpaceAvailable;
    }

    public Integer getTotalChunksMaintained() {
        return totalChunksMaintained;
    }

    public Vector<ChunkMetadata> getChunkMetadata() {
        return chunkMetadata;
    }

    public Timestamp getLastRecordedHeartbeat() {
        return lastRecordedHeartbeat;
    }

    public void recordHeartbeatTimestamp() {
        this.lastRecordedHeartbeat = Timestamp.from(Instant.now());
    }

    public Long timeSinceLastHeartbeat() {
        return Timestamp.from(Instant.now()).getTime() - this.lastRecordedHeartbeat.getTime();
    }

    public boolean isExpired() {
        return timeSinceLastHeartbeat() > (Constants.HEARTBEAT_MINOR_INTERVAL + Constants.HEARTBEAT_GRACE_PERIOD);
    }

    public boolean contains(String filename, Integer sequence) {
        for (ChunkMetadata cm: this.chunkMetadata) {
            if (filename.equals(cm.getAbsoluteFilePath()) && sequence.equals(cm.getSequence())) {
                return true;
            }
        }
        return false;
    }

    public void incrementTotalChunksMaintained() {
        this.totalChunksMaintained++;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (this == other) return true;
        if (!(other instanceof ChunkServerMetadata)) return false;
        ChunkServerMetadata otherChunkServerMetadata = (ChunkServerMetadata) other;
        return this.hostname.equals(otherChunkServerMetadata.hostname);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ChunkServerMetadata:\n");
        sb.append(String.format("  hostname: %s\n", this.hostname));
        sb.append(String.format("  freeSpaceAvailable: %d\n", this.freeSpaceAvailable));
        sb.append(String.format("  totalChunksMaintained: %d\n", this.totalChunksMaintained));
        if (this.chunkMetadata.isEmpty()) {
            sb.append("  chunkMetadata: [ ]\n");
        } else {
            sb.append("  chunkMetadata: [\n");
            for (ChunkMetadata cm: this.chunkMetadata) {
                sb.append(cm);
            }
            sb.append("  ]\n");
        }

        return sb.toString();
    }
}
