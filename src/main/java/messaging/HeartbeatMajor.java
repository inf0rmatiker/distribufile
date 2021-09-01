package messaging;

import util.Host;

import java.net.UnknownHostException;

public class HeartbeatMajor extends Heartbeat {

    public HeartbeatMajor(String hostname, String ipAddress, Integer port, Integer totalChunksMaintained,
                          Long freeSpaceAvailable) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.totalChunksMaintained = totalChunksMaintained;
        this.freeSpaceAvailable = freeSpaceAvailable;
    }

    public HeartbeatMajor(byte[] marshaledBytes) {
        this.marshaledBytes = marshaledBytes;
    }

    @Override
    public MessageType getType() {
        return MessageType.HEARTBEAT_MAJOR;
    }
}