package messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Heartbeat extends Message {

    Logger log = LoggerFactory.getLogger(Heartbeat.class);

    public Integer totalChunksMaintained;
    public Long freeSpaceAvailable;

    public Integer getTotalChunksMaintained() {
        return totalChunksMaintained;
    }

    public Long getFreeSpaceAvailable() {
        return freeSpaceAvailable;
    }

    /**
     * In addition to the header, writes the total chunks maintained and free space available
     * to the DataOutputStream.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal header fields
        dataOutputStream.writeInt(this.totalChunksMaintained);
        dataOutputStream.writeLong(this.freeSpaceAvailable);
    }

    /**
     * In addition to the header, this unmarshals the total chunks maintained and free space available
     * from the DataInputStream.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal header fields
        this.totalChunksMaintained = dataInputStream.readInt();
        this.freeSpaceAvailable = dataInputStream.readLong();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Heartbeat)) return false;
        Heartbeat otherMessage = (Heartbeat) other;
        return (this.getType().equals(otherMessage.getType()) &&
                this.getHostname().equals(otherMessage.getHostname()) &&
                this.getIpAddress().equals(otherMessage.getIpAddress()) &&
                this.getPort().equals(otherMessage.getPort()) &&
                this.getTotalChunksMaintained().equals(otherMessage.getTotalChunksMaintained()) &&
                this.getFreeSpaceAvailable().equals(otherMessage.getFreeSpaceAvailable())
        );
    }
}