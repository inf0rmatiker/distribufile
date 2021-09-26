package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class ClientReadResponse extends Message  {

    // The name of the file we are requesting to read
    public String absoluteFilePath;

    // List of Chunk Server hostnames where we can retrieve each of the chunks for a file
    // The String at the ith index is the hostname of the Chunk Server storing chunk i
    List<String> chunkServerHostnames;

    // Confirmation of the existence of the requested file
    public Boolean fileExists;

    public ClientReadResponse(String hostname, String ipAddress, Integer port, String absoluteFilePath,
                              List<String> chunkServerHostnames, Boolean fileExists) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absoluteFilePath;
        this.chunkServerHostnames = chunkServerHostnames;
        this.fileExists = fileExists;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ClientReadResponse: {}", e.getMessage());
        }
    }

    public ClientReadResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CLIENT_READ_RESPONSE;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public List<String> getChunkServerHostnames() {
        return chunkServerHostnames;
    }

    public Boolean getFileExists() {
        return fileExists;
    }

    /**
     * In addition to the header, writes the absolute path of the file being requested for reading,
     * the hostnames of the Chunk Servers holding its chunks, and whether we were able to locate the file.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to write to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeString(dataOutputStream, this.absoluteFilePath);
        writeStringList(dataOutputStream, this.chunkServerHostnames);
        dataOutputStream.writeBoolean(this.fileExists);
    }

    /**
     * In addition to the header, reads the absolute path of the file being requested for reading,
     * the hostnames of the Chunk Servers holding its chunks, and whether we were able to locate the file.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.absoluteFilePath = readString(dataInputStream);
        this.chunkServerHostnames = readStringList(dataInputStream);
        this.fileExists = dataInputStream.readBoolean();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ClientReadResponse)) return false;
        ClientReadResponse crrOther = (ClientReadResponse) other;
        return this.absoluteFilePath.equals(crrOther.getAbsoluteFilePath()) &&
                this.chunkServerHostnames.equals(crrOther.getChunkServerHostnames()) &&
                this.fileExists.equals(crrOther.getFileExists());
    }

    @Override
    public String toString() {
        return "> ClientReadResponse:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  chunkServerHostnames: %s\n", this.chunkServerHostnames) +
                String.format("  fileExists: %b\n", this.fileExists);
    }
}
