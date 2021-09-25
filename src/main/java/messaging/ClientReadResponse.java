package messaging;

import java.io.DataInputStream;
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
