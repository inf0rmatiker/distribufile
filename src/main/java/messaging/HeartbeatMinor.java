package messaging;

import java.net.UnknownHostException;

public class HeartbeatMinor extends Heartbeat {

    public String[] newlyAddedChunks, corruptedFiles;

    public HeartbeatMinor(Integer totalChunksMaintained, Long freeSpaceAvailable, String[] newlyAddedChunks,
                          String[] corruptedFiles) throws UnknownHostException {
        super(MessageType.HEARTBEAT_MINOR, totalChunksMaintained, freeSpaceAvailable);
        this.newlyAddedChunks = newlyAddedChunks;
        this.corruptedFiles = corruptedFiles;
    }

    public HeartbeatMinor(String hostname, String ipAddress, Integer port, Integer totalChunksMaintained,
                          Long freeSpaceAvailable, String[] newlyAddedChunks, String[] corruptedFiles) {
        super(MessageType.HEARTBEAT_MINOR, hostname, ipAddress, port, totalChunksMaintained, freeSpaceAvailable);
        this.newlyAddedChunks = newlyAddedChunks;
        this.corruptedFiles = corruptedFiles;
    }

    public String[] getNewlyAddedChunks() {
        return newlyAddedChunks;
    }

    public String[] getCorruptedFiles() {
        return corruptedFiles;
    }
}