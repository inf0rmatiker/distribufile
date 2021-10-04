package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A Message concrete class which functions as a response from the controller.Controller to a Client's write request.
 * It provides a list of Chunk Servers to write the chunk to.
 */
public class ClientWriteResponse extends ChunkMessage {

    // A list of chunk server hostnames that the chunk should be written to
    public List<String> replicationChunkServers;

    public ClientWriteResponse(String hostname, String ipAddress, Integer port, List<String> replicationChunkServers,
                               String absoluteFilePath, Integer sequence) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.replicationChunkServers = replicationChunkServers;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ClientWriteResponse: {}", e.getMessage());
        }
    }

    public ClientWriteResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CLIENT_WRITE_RESPONSE;
    }

    public List<String> getReplicationChunkServers() {
        return replicationChunkServers;
    }

    /**
     * In addition to the header, chunk filename, and sequence, writes the list of chunk servers to write the chunk to.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to read to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeStringList(dataOutputStream, this.replicationChunkServers);
    }

    /**
     * In addition to the header, chunk filename, and sequence, reads the list of chunk servers to write the chunk to.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.replicationChunkServers = readStringList(dataInputStream);
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

    @Override
    public String toString() {
        return "> ClientWriteResponse:\n" +
                String.format("  replicationChunkServers: %s\n", this.replicationChunkServers) +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  sequence: %d\n", this.sequence);
    }
}
