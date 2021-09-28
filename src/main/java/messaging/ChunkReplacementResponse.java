package messaging;

import chunkserver.Chunk;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Similar to ChunkReadResponse, but intended as a response to a ChunkReplacementRequest when a Chunk Server
 * fails an integrity check on a chunk.
 */
public class ChunkReplacementResponse extends ChunkReadResponse {

    public ChunkReplacementResponse(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence,
                                    Chunk chunk, List<String> chunkReplacements) {
        super(hostname, ipAddress, port, absoluteFilePath, sequence, chunk, chunkReplacements);
    }

    public ChunkReplacementResponse(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_REPLACEMENT_RESPONSE;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkReplacementResponse)) return false;
        ChunkReplacementResponse crrOther = (ChunkReplacementResponse) other;
        return this.absoluteFilePath.equals(crrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(crrOther.getSequence());
    }

    @Override
    public String toString() {
        return "> ChunkReplacementResponse:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  sequence: %d\n", this.sequence) +
                String.format("  chunk: %s\n", this.chunk) +
                String.format("  chunkReplacements: %b\n", this.chunkReplacements);
    }
}
