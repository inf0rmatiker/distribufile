package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class ChunkStoreRequest extends Message {

    // A list of chunk server hostnames to forward this message to for chunk replication
    public List<String> replicationChunkServers;

    // The absolute path of the file to which this chunk belongs
    public String absoluteFilePath;

    // The sequence or index number of the chunk within the file
    public Integer sequence;

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
    }

    public ChunkStoreRequest(byte[] marshaledBytes) {
        this.marshaledBytes = marshaledBytes;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_STORE_REQUEST;
    }

    public List<String> getReplicationChunkServers() {
        return replicationChunkServers;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public Integer getSequence() {
        return sequence;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    /**
     * In addition to the header, writes chunk metadata for each of
     * the chunks the chunk server is managing.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to read to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
    }

    /**
     * In addition to the header, this unmarshals the list of
     * chunk metadata objects that the chunk server is managing.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
    }
}
