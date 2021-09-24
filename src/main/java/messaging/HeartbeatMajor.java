package messaging;

import chunkserver.ChunkMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class HeartbeatMajor extends Heartbeat {

    Logger log = LoggerFactory.getLogger(HeartbeatMajor.class);

    public List<ChunkMetadata> chunksMetadata;

    public HeartbeatMajor(String hostname, String ipAddress, Integer port, Integer totalChunksMaintained,
                          Long freeSpaceAvailable, List<ChunkMetadata> chunksMetadata) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.totalChunksMaintained = totalChunksMaintained;
        this.freeSpaceAvailable = freeSpaceAvailable;
        this.chunksMetadata = chunksMetadata;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal HeartbeatMajor: {}", e.getMessage());
        }
    }

    public HeartbeatMajor(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }


    @Override
    public MessageType getType() {
        return MessageType.HEARTBEAT_MAJOR;
    }

    public List<ChunkMetadata> getChunksMetadata() {
        return chunksMetadata;
    }

    /**
     * In addition to the header, total chunks maintained, and free space available, writes chunk metadata for each of
     * the chunks the chunk server is managing.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Heartbeat fields
        writeChunkMetadataList(dataOutputStream, this.chunksMetadata);
    }

    /**
     * In addition to the header, the total chunks maintained, and free space available, this unmarshals the list of
     * chunk metadata objects that the chunk server is managing.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Heartbeat fields
        this.chunksMetadata = readChunkMetadataList(dataInputStream);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof HeartbeatMajor)) return false;
        HeartbeatMajor otherMessage = (HeartbeatMajor) other;
        return (this.getType().equals(otherMessage.getType()) &&
                this.getHostname().equals(otherMessage.getHostname()) &&
                this.getIpAddress().equals(otherMessage.getIpAddress()) &&
                this.getPort().equals(otherMessage.getPort()) &&
                this.getTotalChunksMaintained().equals(otherMessage.getTotalChunksMaintained()) &&
                this.getFreeSpaceAvailable().equals(otherMessage.getFreeSpaceAvailable()) &&
                this.getChunksMetadata().equals(otherMessage.getChunksMetadata())
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("> HeartbeatMajor:");
        sb.append(String.format("\n  totalChunksMaintained: %d", this.totalChunksMaintained));
        sb.append(String.format("\n  freeSpaceAvailable: %d", this.freeSpaceAvailable));
        for (ChunkMetadata cm: this.chunksMetadata) {
            sb.append(String.format("\n%s", cm));
        }
        return sb.toString();
    }
}