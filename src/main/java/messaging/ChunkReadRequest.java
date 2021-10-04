package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkReadRequest extends ChunkMessage {

    public ChunkReadRequest(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ChunkReadRequest: {}", e.getMessage());
        }
    }

    public ChunkReadRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_READ_REQUEST;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkReadRequest)) return false;
        ChunkReadRequest crrOther = (ChunkReadRequest) other;
        return this.absoluteFilePath.equals(crrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(crrOther.getSequence());
    }

    @Override
    public String toString() {
        return "> ChunkReadRequest:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  sequence: %d\n", this.sequence);
    }
}
