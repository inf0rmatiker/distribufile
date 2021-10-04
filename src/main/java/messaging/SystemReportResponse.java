package messaging;

import controller.ChunkServerMetadata;
import controller.FileMetadata;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Response Message to a SystemReportRequest, received by the Controller. Contains all metadata information
 * currently held about tracked files and Chunk Servers.
 */
public class SystemReportResponse extends Message {

    public List<FileMetadata> trackedFileMetadata;

    public SystemReportResponse(String hostname, String ipAddress, Integer port) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal SystemReportResponse: {}", e.getMessage());
        }
    }

    public SystemReportResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.SYSTEM_REPORT_RESPONSE;
    }

    public List<FileMetadata> getTrackedFileMetadata() {
        return trackedFileMetadata;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        return (other instanceof SystemReportResponse);
    }

    @Override
    public String toString() {
        return "> SystemReportResponse";
    }
}
