package messaging;

import chunkserver.Chunk;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Similar to ChunkReadResponse, but intended as a response to a ChunkReplacementRequest when a Chunk Server
 * fails an integrity check on a chunk.
 */
public class ChunkReplacementResponse extends ChunkReadResponse {

    public ChunkReplacementResponse(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence,
                                    Chunk chunk, Boolean integrityVerified) {
        super(hostname, ipAddress, port, absoluteFilePath, sequence, chunk, integrityVerified);
    }

    public ChunkReplacementResponse(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return null;
    }
}
