package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Command from the Controller to a Chunk Server to send one of its chunks
 * to a target Chunk Server for replication. Happens when the replication of a chunk
 * falls below a certain level due to Chunk Server failure.
 */
public class ChunkReplicateCommand extends ChunkMessage {

    // The hostname of the Chunk Server the chunk replica should be stored at
    public String targetChunkServer;

    public ChunkReplicateCommand(String hostname, String ipAddress, Integer port, String targetChunkServer,
                                 String absoluteFilePath, Integer sequence) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.targetChunkServer = targetChunkServer;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
        try {
            this.marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ChunkReplicateCommand: {}", e.getMessage());
        }
    }

    public ChunkReplicateCommand(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_REPLICATE_COMMAND;
    }

    public String getTargetChunkServer() {
        return targetChunkServer;
    }

    /**
     * In addition to the header, filename, and sequence of the chunk, writes the target Chunk Server's hostname where
     * the replica should be stored.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to write to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeString(dataOutputStream, this.targetChunkServer);

    }

    /**
     * In addition to the header, filename, and sequence of the chunk, reads the Chunk Server's hostname where another
     * replica should be stored.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header=
        this.targetChunkServer = readString(dataInputStream);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkReplicateCommand)) return false;
        ChunkReplicateCommand crcOther = (ChunkReplicateCommand) other;
        return (this.absoluteFilePath.equals(crcOther.getAbsoluteFilePath()) &&
                this.sequence.equals(crcOther.getSequence()) &&
                this.targetChunkServer.equals(crcOther.getTargetChunkServer()));
    }

    @Override
    public String toString() {
        return "ChunkReplicateCommand:" +
                String.format("\n  absoluteFilePath: %s", this.absoluteFilePath) +
                String.format("\n  sequence: %d", this.sequence) +
                String.format("\n  targetChunkServer: %s", this.targetChunkServer);
    }


}
