package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ChunkStoreResponse extends Message {

    // The absolute path of the file to which the chunk belongs
    public String absoluteFilePath;

    // The sequence number of the chunk within the file
    public Integer sequence;

    // Flag indicating success or failure for chunk storage/replication
    public Boolean success;

    public ChunkStoreResponse(String hostname, String ipAddress, Integer port, String absolutePath, Integer sequence,
                              Boolean success) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absolutePath;
        this.sequence = sequence;
        this.success = success;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ChunkStoreResponse: {}", e.getMessage());
        }
    }

    public ChunkStoreResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_STORE_RESPONSE;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public Integer getSequence() {
        return sequence;
    }

    public Boolean getSuccess() {
        return success;
    }

    /**
     * In addition to the header, writes the chunk's filename, sequence, and flag
     * indicating storage success or failure.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to read to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeString(dataOutputStream, this.absoluteFilePath);
        dataOutputStream.writeInt(this.sequence);
        dataOutputStream.writeBoolean(this.success);
    }

    /**
     * In addition to the header, reads the chunk's filename, sequence, and flag
     * indicating storage success or failure.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.absoluteFilePath = readString(dataInputStream);
        this.sequence = dataInputStream.readInt();
        this.success = dataInputStream.readBoolean();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkStoreResponse)) return false;
        ChunkStoreResponse csrOther = (ChunkStoreResponse) other;
        return (this.absoluteFilePath.equals(csrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(csrOther.getSequence()) &&
                this.success.equals(csrOther.getSuccess())
        );
    }

    @Override
    public String toString() {
        return "ChunkStoreResponse:" +
                String.format("\n  absoluteFilePath: %s", this.absoluteFilePath) +
                String.format("\n  sequence: %d", this.sequence) +
                String.format("\n  success: %b", this.success);
    }
}
