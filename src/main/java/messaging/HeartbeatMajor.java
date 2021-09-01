package messaging;

import util.Host;

import java.net.UnknownHostException;

public class HeartbeatMajor extends Heartbeat {

    public HeartbeatMajor(Integer totalChunksMaintained, Long freeSpaceAvailable) throws UnknownHostException {
        this(Host.getHostname(), Host.getIpAddress(), 9001, totalChunksMaintained, freeSpaceAvailable);
    }

    public HeartbeatMajor(String hostname, String ipAddress, Integer port, Integer totalChunksMaintained,
                          Long freeSpaceAvailable) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.totalChunksMaintained = totalChunksMaintained;
        this.freeSpaceAvailable = freeSpaceAvailable;
    }

    @Override
    public MessageType getType() {
        return MessageType.HEARTBEAT_MAJOR;
    }

    public HeartbeatMajor(byte[] marshaledBytes) {
        super(marshaledBytes);
    }
}