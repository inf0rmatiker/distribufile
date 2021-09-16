package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A Message concrete class which should be targeted at the controller.Controller to request a list of Chunk Servers
 * to write a chunk to.
 */
public class ClientWriteRequest extends Message {

    public String absoluteFilePath;
    public Integer sequence;

    public ClientWriteRequest(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
    }

    public ClientWriteRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CLIENT_WRITE_REQUEST;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public Integer getSequence() {
        return sequence;
    }

    /**
     * In addition to the header, writes the absolute path of the file for the chunk we
     * are requesting to write, and the sequence number of the chunk within that file.
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
     * In addition to the header, reads the absolute path of the file for the chunk being
     * requested for writing, and the sequence number of the chunk within that file.
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
        if (!(other instanceof ClientWriteRequest)) return false;
        ClientWriteRequest cwrOther = (ClientWriteRequest) other;
        return (this.absoluteFilePath.equals(cwrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(cwrOther.getSequence())
        );
    }

}
