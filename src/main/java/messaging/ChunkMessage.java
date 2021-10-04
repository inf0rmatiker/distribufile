package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Abstract Message type encompassing common information shared
 * by all Message concrete classes which relate to a specific Chunk:
 * That is, the identifying information of the absolute file path
 * to which the chunk belongs to, and the sequence number of the chunk.
 */
public abstract class ChunkMessage extends Message {

    // Absolute path of the file, as seen by the Client
    public String absoluteFilePath;

    // Sequence index of the chunk within the file
    public Integer sequence;

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
}
