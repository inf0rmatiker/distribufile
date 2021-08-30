package messaging;

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

    public Integer getTotalChunksMaintained() {
        return totalChunksMaintained;
    }

    public Long getFreeSpaceAvailable() {
        return freeSpaceAvailable;
    }


}