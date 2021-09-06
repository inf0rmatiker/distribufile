package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A Message concrete class which functions as a response from the Controller to a Client's write request.
 * It provides a list of Chunk Servers to write the chunk to.
 */
public class ClientWriteResponse extends Message {

    // A list of chunk server hostnames that the chunk should be written to
    public List<String> replicationChunkServers;

    // Absolute path of the file to which the chunk belongs
    public String absoluteFilePath;

    // Sequence number/index of the chunk within the file
    public Integer sequence;


    public ClientWriteResponse(String hostname, String ipAddress, Integer port, List<String> replicationChunkServers,
                               String absoluteFilePath, Integer sequence) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.replicationChunkServers = replicationChunkServers;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
    }

    public ClientWriteResponse(byte[] marshaledBytes) {
        this.marshaledBytes = marshaledBytes;
    }

    @Override
    public MessageType getType() {
        return MessageType.CLIENT_WRITE_RESPONSE;
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

    /**
     * In addition to the header, writes the list of chunk servers to write the chunk to,
     * and the original chunk filename and sequence.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to read to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeStringList(dataOutputStream, this.replicationChunkServers);
        writeString(dataOutputStream, this.absoluteFilePath);
        dataOutputStream.writeInt(this.sequence);
    }

    /**
     * In addition to the header, reads the list of chunk servers to write the chunk to,
     * and the original chunk filename and sequence.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.replicationChunkServers = readStringList(dataInputStream);
        this.absoluteFilePath = readString(dataInputStream);
        this.sequence = dataInputStream.readInt();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ClientWriteResponse)) return false;
        ClientWriteResponse cwrOther = (ClientWriteResponse) other;
        return (this.replicationChunkServers.equals(cwrOther.getReplicationChunkServers()) &&
                this.absoluteFilePath.equals(cwrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(cwrOther.getSequence())
        );
    }
}