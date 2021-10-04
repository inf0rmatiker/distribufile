package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A Message concrete class which should be targeted at the Controller to request a list of Chunk Servers
 * to write a chunk to.
 */
public class ClientWriteRequest extends ChunkMessage {

    public ClientWriteRequest(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ClientWriteRequest: {}", e.getMessage());
        }
    }

    public ClientWriteRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CLIENT_WRITE_REQUEST;
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

    @Override
    public String toString() {
        return "> ClientWriteRequest:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  sequence: %d\n", this.sequence);
    }

}
