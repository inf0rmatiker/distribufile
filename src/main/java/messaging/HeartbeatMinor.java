package messaging;

import chunkserver.Chunk;
import chunkserver.ChunkMetadata;
import util.Host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class HeartbeatMinor extends Heartbeat {

    Logger log = LoggerFactory.getLogger(HeartbeatMinor.class);

    public List<ChunkMetadata> newlyAddedChunks, corruptedChunks;

    public HeartbeatMinor(String hostname, String ipAddress, Integer port, Integer totalChunksMaintained,
                          Long freeSpaceAvailable, List<ChunkMetadata> newlyAddedChunks, List<ChunkMetadata> corruptedChunks) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.totalChunksMaintained = totalChunksMaintained;
        this.freeSpaceAvailable = freeSpaceAvailable;
        this.newlyAddedChunks = newlyAddedChunks;
        this.corruptedChunks = corruptedChunks;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal HeartbeatMinor: {}", e.getMessage());
        }
    }

    public HeartbeatMinor(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.HEARTBEAT_MINOR;
    }

    public List<ChunkMetadata> getNewlyAddedChunks() {
        return newlyAddedChunks;
    }

    public List<ChunkMetadata> getCorruptedChunks() {
        return corruptedChunks;
    }


    /**
     * In addition to the header, total chunks maintained, and free space available, writes any newly added chunks
     * and recently corrupted files to the DataOutputStream.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If unable to write to stream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Heartbeat fields
        writeChunkMetadataList(dataOutputStream, this.newlyAddedChunks);
        writeChunkMetadataList(dataOutputStream, this.corruptedChunks);
    }

    /**
     * In addition to the header, the total chunks maintained, and free space available, this unmarshals the newly
     * added chunks string array and corrupted files string array from the DataInputStream.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If unable to read from stream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Heartbeat fields
        this.newlyAddedChunks = readChunkMetadataList(dataInputStream);
        this.corruptedChunks = readChunkMetadataList(dataInputStream);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HeartbeatMinor)) return false;
        HeartbeatMinor otherMessage = (HeartbeatMinor) other;
        return (this.getType().equals(otherMessage.getType()) &&
                this.getHostname().equals(otherMessage.getHostname()) &&
                this.getIpAddress().equals(otherMessage.getIpAddress()) &&
                this.getPort().equals(otherMessage.getPort()) &&
                this.getTotalChunksMaintained().equals(otherMessage.getTotalChunksMaintained()) &&
                this.getFreeSpaceAvailable().equals(otherMessage.getFreeSpaceAvailable()) &&
                this.getNewlyAddedChunks().equals(otherMessage.getNewlyAddedChunks()) &&
                this.getCorruptedChunks().equals(otherMessage.getCorruptedChunks())
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HeartbeatMinor:\n");
        sb.append(String.format("  freeSpaceAvailable: %d\n", this.freeSpaceAvailable));
        sb.append(String.format("  totalChunksMaintained: %d\n", this.totalChunksMaintained));

        if (this.newlyAddedChunks.isEmpty()) {
            sb.append("  newlyAddedChunks: [ ]\n");
        } else {
            sb.append("  newlyAddedChunks: [\n");
            for (ChunkMetadata cm: this.newlyAddedChunks) {
                sb.append(cm);
            }
            sb.append("  ]\n");
        }

        if (this.corruptedChunks.isEmpty()) {
            sb.append("  corruptedChunks: [ ]\n");
        } else {
            sb.append("  corruptedChunks: [\n");
            for (ChunkMetadata cm: this.corruptedChunks) {
                sb.append(cm);
            }
            sb.append("  ]\n");
        }

        return sb.toString();
    }

}