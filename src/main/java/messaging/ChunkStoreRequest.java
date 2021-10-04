package messaging;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * A Message concrete class which allows the Client to send a chunk to a Chunk Server, along with the list of
 * additional Chunk Servers to replicate/forward the chunk to.
 */
public class ChunkStoreRequest extends ChunkMessage {

    // A list of chunk server hostnames to forward this message to for chunk replication
    public List<String> replicationChunkServers;

    // The raw chunk data in bytes
    public byte[] chunkData;

    public ChunkStoreRequest(String hostname, String ipAddress, Integer port, List<String> replicationChunkServers,
                             String absoluteFilePath, Integer sequence, byte[] chunkData) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.replicationChunkServers = replicationChunkServers;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
        this.chunkData = chunkData;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ChunkStoreRequest: {}", e.getMessage());
        }
    }

    public ChunkStoreRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_STORE_REQUEST;
    }

    public List<String> getReplicationChunkServers() {
        return replicationChunkServers;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    /**
     * Removes and returns the last chunk server host in the list of replication recipients.
     * @return The host name of a chunk server which was just removed from the end of the list.
     */
    public String popReplicationRecipient() {
        return this.replicationChunkServers.remove(this.replicationChunkServers.size() - 1);
    }

    /**
     * In addition to the header, chunk filename, and sequence, writes a list of recipients and a chunk
     * to replicate.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to read to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeStringList(dataOutputStream, this.replicationChunkServers);

        // Write chunk data
        dataOutputStream.writeInt(this.chunkData.length);
        dataOutputStream.write(this.chunkData);
    }

    /**
     * In addition to the header, chunk filename, and sequence, reads a list of recipients and a chunk
     * to replicate.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.replicationChunkServers = readStringList(dataInputStream);

        // Read chunk data
        int chunkSize = dataInputStream.readInt();
        this.chunkData = new byte[chunkSize];
        dataInputStream.readFully(this.chunkData, 0, chunkSize);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkStoreRequest)) return false;
        ChunkStoreRequest csrOther = (ChunkStoreRequest) other;
        return (this.replicationChunkServers.equals(csrOther.getReplicationChunkServers()) &&
                this.absoluteFilePath.equals(csrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(csrOther.getSequence()) &&
                Arrays.equals(this.chunkData, csrOther.getChunkData())
        );
    }

    @Override
    public String toString() {
        return "> ChunkStoreRequest:" +
                String.format("\n  replicationChunkServers: %s", this.replicationChunkServers) +
                String.format("\n  absoluteFilePath: %s", this.absoluteFilePath) +
                String.format("\n  sequence: %d", this.sequence) +
                String.format("\n  chunkData: [ --- byte array of size %d --- ]", this.chunkData.length);
    }
}
