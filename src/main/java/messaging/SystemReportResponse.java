package messaging;

import controller.ChunkServerMetadata;
import controller.FileMetadata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Response Message to a SystemReportRequest, received by the Controller. Contains all metadata information
 * currently held about tracked files and Chunk Servers.
 */
public class SystemReportResponse extends Message {

    public List<FileMetadata> trackedFileMetadata;

    public SystemReportResponse(String hostname, String ipAddress, Integer port, List<FileMetadata> trackedFileMetadata) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.trackedFileMetadata = trackedFileMetadata;
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

    /**
     * In addition to the header, writes the tracked FileMetadata objects
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to write to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeFileMetadataList(dataOutputStream, this.trackedFileMetadata);
    }

    /**
     * In addition to the header, reads the tracked FileMetadata objects
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.trackedFileMetadata = readFileMetadataList(dataInputStream);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        return (other instanceof SystemReportResponse);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("> SystemReportResponse");
        for (FileMetadata fm: trackedFileMetadata) {
            sb.append(fm);
            sb.append("\n");
        }
        return sb.toString();
    }
}
