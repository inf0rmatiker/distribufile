package messaging;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

public class Heartbeat extends Message {

    public Integer totalChunksMaintained;
    public Long freeSpaceAvailable;

    public Heartbeat(MessageType type, Integer totalChunksMaintained, Long freeSpaceAvailable) throws UnknownHostException {
        super(type);
        this.totalChunksMaintained = totalChunksMaintained;
        this.freeSpaceAvailable = freeSpaceAvailable;
    }

    public Heartbeat(MessageType type, String hostname, String ipAddress, Integer port, Integer totalChunksMaintained,
                     Long freeSpaceAvailable) {
        super(type, hostname, ipAddress, port);
        this.totalChunksMaintained = totalChunksMaintained;
        this.freeSpaceAvailable = freeSpaceAvailable;
    }

    public Heartbeat(byte[] marshaledBytes) {
        super(marshaledBytes);
    }

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