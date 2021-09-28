package messaging;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Similar to a ChunkReadRequest, but differs in that this originates from a Chunk Server as a result of
 * a failed chunk integrity check. The response to this should not be a ChunkReadResponse, rather it should
 * be a ChunkReplacementResponse which contains an entire marshalled Chunk object, integrity and metadata info included.
 */
public class ChunkReplacementRequest extends ChunkReadRequest {

    public ChunkReplacementRequest(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence) {
        super(hostname, ipAddress, port, absoluteFilePath, sequence);
    }

    public ChunkReplacementRequest(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_REPLACEMENT_REQUEST;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkReplacementRequest)) return false;
        ChunkReplacementRequest crrOther = (ChunkReplacementRequest) other;
        return this.absoluteFilePath.equals(crrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(crrOther.getSequence());
    }

    @Override
    public String toString() {
        return "> ChunkReplacementRequest:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  sequence: %d\n", this.sequence);
    }

}
