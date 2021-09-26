package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkReadResponse extends Message {

    public String absoluteFilePath;

    public Integer sequence;

    public byte[] chunk;

    public Boolean integrityVerified;

    public ChunkReadResponse(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence,
                             byte[] chunk, Boolean integrityVerified) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
        this.chunk = chunk;
        this.integrityVerified = integrityVerified;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ChunkReadResponse: {}", e.getMessage());
        }
    }

    public ChunkReadResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_READ_RESPONSE;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public Integer getSequence() {
        return sequence;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public Boolean getIntegrityVerified() {
        return integrityVerified;
    }

    /**
     * In addition to the header, writes the chunk's filename and sequence.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to read to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeString(dataOutputStream, this.absoluteFilePath);
        dataOutputStream.writeInt(this.sequence);

        dataOutputStream.writeInt(this.chunk.length);
        dataOutputStream.write(this.chunk);

        dataOutputStream.writeBoolean(this.integrityVerified);
    }

    /**
     * In addition to the header, reads the chunk's filename and sequence.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.absoluteFilePath = readString(dataInputStream);
        this.sequence = dataInputStream.readInt();

        int chunkLength = dataInputStream.readInt();
        this.chunk = dataInputStream.readNBytes(chunkLength);

        this.integrityVerified = dataInputStream.readBoolean();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkReadResponse)) return false;
        ChunkReadResponse crrOther = (ChunkReadResponse) other;
        return this.absoluteFilePath.equals(crrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(crrOther.getSequence());
    }

    @Override
    public String toString() {
        return "> ChunkReadResponse:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  sequence: %d\n", this.sequence) +
                String.format("  chunk: [ --- byte array of size %d --- ]\n", this.chunk.length) +
                String.format("  integrityVerified: %b\n", this.integrityVerified);
    }
}
