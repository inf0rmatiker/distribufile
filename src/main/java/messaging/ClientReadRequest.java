package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientReadRequest extends Message {

    // The name of the file we are requesting to read
    public String absoluteFilePath;

    public ClientReadRequest(String hostname, String ipAddress, Integer port, String absoluteFilePath) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absoluteFilePath;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ClientReadRequest: {}", e.getMessage());
        }
    }

    public ClientReadRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CLIENT_READ_REQUEST;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    /**
     * In addition to the header, writes the absolute path of the file being requested for reading.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to write to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeString(dataOutputStream, this.absoluteFilePath);
    }

    /**
     * In addition to the header, reads the absolute path of the file being requested for reading.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.absoluteFilePath = readString(dataInputStream);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ClientReadRequest)) return false;
        ClientReadRequest crrOther = (ClientReadRequest) other;
        return this.absoluteFilePath.equals(crrOther.getAbsoluteFilePath());
    }

    @Override
    public String toString() {
        return "> ClientReadRequest:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath);
    }
}
