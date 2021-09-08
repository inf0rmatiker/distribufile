package distribufile.messaging;

import distribufile.util.Host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class HeartbeatMinor extends Heartbeat {

    Logger log = LoggerFactory.getLogger(HeartbeatMinor.class);

    public String[] newlyAddedChunks, corruptedFiles;

    public HeartbeatMinor(String hostname, String ipAddress, Integer port, Integer totalChunksMaintained,
                          Long freeSpaceAvailable, String[] newlyAddedChunks, String[] corruptedFiles) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.totalChunksMaintained = totalChunksMaintained;
        this.freeSpaceAvailable = freeSpaceAvailable;
        this.newlyAddedChunks = newlyAddedChunks;
        this.corruptedFiles = corruptedFiles;
    }

    public HeartbeatMinor(byte[] marshaledBytes) {
        this.marshaledBytes = marshaledBytes;
    }

    @Override
    public MessageType getType() {
        return MessageType.HEARTBEAT_MINOR;
    }

    public String[] getNewlyAddedChunks() {
        return newlyAddedChunks;
    }

    public String[] getCorruptedFiles() {
        return corruptedFiles;
    }


    /**
     * In addition to the header, total chunks maintained, and free space available, writes any newly added chunks
     * and recently corrupted files to the DataOutputStream.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Heartbeat fields
        writeStringArray(dataOutputStream, this.newlyAddedChunks);
        writeStringArray(dataOutputStream, this.corruptedFiles);
    }

    /**
     * In addition to the header, the total chunks maintained, and free space available, this unmarshals the newly
     * added chunks string array and corrupted files string array from the DataInputStream.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Heartbeat fields
        this.newlyAddedChunks = readStringArray(dataInputStream);
        this.corruptedFiles = readStringArray(dataInputStream);
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
                Arrays.equals(this.getNewlyAddedChunks(), otherMessage.getNewlyAddedChunks()) &&
                Arrays.equals(this.getCorruptedFiles(), otherMessage.getCorruptedFiles())
        );
    }

}