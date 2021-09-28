package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Message returned from the Controller in response to a HeartbeatMinor Message for corrupted chunks.
 * Tells the initiating Chunk Server where to find another replica of the chunk.
 */
public class ChunkReplicationInfo extends Message {

    public String replicationChunkServer;

    public ChunkReplicationInfo(String hostname, String ipAddress, Integer port, String replicationChunkServer) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.replicationChunkServer = replicationChunkServer;
        try {
            this.marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ChunkReplicationInfo: {}", e.getMessage());
        }
    }

    public ChunkReplicationInfo(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_REPLICATION_INFO;
    }

    public String getReplicationChunkServer() {
        return replicationChunkServer;
    }

    /**
     * In addition to the header, writes the Chunk Server's hostname where another replica is stored.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to write to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeString(dataOutputStream, this.replicationChunkServer);
    }

    /**
     * In addition to the header, reads the Chunk Server's hostname where another replica is stored.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.replicationChunkServer = readString(dataInputStream);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkReplicationInfo)) return false;
        ChunkReplicationInfo criOther = (ChunkReplicationInfo) other;
        return (this.replicationChunkServer.equals(criOther.getReplicationChunkServer()));
    }

    @Override
    public String toString() {
        return "ChunkReplicationInfo:" +
                String.format("\n  replicationChunkServer: %s", this.replicationChunkServer);
    }
}
