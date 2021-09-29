package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Command from the Controller to a Chunk Server to send one of its chunks
 * to a target Chunk Server for replication. Happens when the replication of a chunk
 * falls below a certain level due to Chunk Server failure.
 */
public class ChunkReplicateCommand extends Message {

    // The hostname of the Chunk Server the chunk replica should be stored at
    public String targetChunkServer;

    // The absolute path of the chunk that needs to be replicated
    public String absoluteFilePath;

    // The sequence index of the chunk that needs to be replicated
    public Integer sequence;

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

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public Integer getSequence() {
        return sequence;
    }

    /**
     * In addition to the header, writes the target Chunk Server's hostname where the replica should be stored,
     * as well as the chunk filename and sequence that should be replicated
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to write to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeString(dataOutputStream, this.targetChunkServer);
        writeString(dataOutputStream, this.absoluteFilePath);
        dataOutputStream.writeInt(this.sequence);
    }

    /**
     * In addition to the header, reads the Chunk Server's hostname where another replica is stored.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.targetChunkServer = readString(dataInputStream);
        this.absoluteFilePath = readString(dataInputStream);
        this.sequence = dataInputStream.readInt();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkReplicateCommand)) return false;
        ChunkReplicateCommand crcOther = (ChunkReplicateCommand) other;
        return (this.targetChunkServer.equals(crcOther.getTargetChunkServer()) &&
                this.absoluteFilePath.equals(crcOther.getAbsoluteFilePath()) &&
                this.sequence.equals(crcOther.getSequence()));
    }

    @Override
    public String toString() {
        return "ChunkReplicateCommand:" +
                String.format("\n  targetChunkServer: %s", this.targetChunkServer) +
                String.format("\n  absoluteFilePath: %s", this.absoluteFilePath) +
                String.format("\n  sequence: %d", this.sequence);
    }


}
