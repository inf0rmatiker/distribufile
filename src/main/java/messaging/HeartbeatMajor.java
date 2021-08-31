package messaging;

import java.net.UnknownHostException;

public class HeartbeatMajor extends Heartbeat {

    public HeartbeatMajor(Integer totalChunksMaintained, Long freeSpaceAvailable) throws UnknownHostException {
        super(MessageType.HEARTBEAT_MAJOR, totalChunksMaintained, freeSpaceAvailable);
    }

    public HeartbeatMajor(String hostname, String ipAddress, Integer port, Integer totalChunksMaintained,
                          Long freeSpaceAvailable) {
        super(MessageType.HEARTBEAT_MAJOR, hostname, ipAddress, port, totalChunksMaintained, freeSpaceAvailable);
    }

    public HeartbeatMajor(byte[] marshaledBytes) {
        super(marshaledBytes);
    }
}