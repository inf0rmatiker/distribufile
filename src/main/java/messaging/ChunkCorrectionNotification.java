package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Notifies the Controller of a completed chunk correction.
 */
public class ChunkCorrectionNotification extends Message {

    public String absoluteFilePath;

    public Integer sequence;

    public ChunkCorrectionNotification(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ChunkCorrectionNotification: {}", e.getMessage());
        }
    }

    public ChunkCorrectionNotification(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_CORRECTION_NOTIFICATION;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public Integer getSequence() {
        return sequence;
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
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkCorrectionNotification)) return false;
        ChunkCorrectionNotification crrOther = (ChunkCorrectionNotification) other;
        return this.absoluteFilePath.equals(crrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(crrOther.getSequence());
    }

    @Override
    public String toString() {
        return "> ChunkCorrectionNotification:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  sequence: %d\n", this.sequence);
    }

}
