package messaging;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Request Message for getting a status report on the state of the filesystem.
 * Sent to the Controller from the Client, and the Controller responds with a SystemReportResponse Message.
 */
public class SystemReportRequest extends Message {

    public SystemReportRequest(String hostname, String ipAddress, Integer port) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal SystemReportRequest: {}", e.getMessage());
        }
    }

    public SystemReportRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.SYSTEM_REPORT_REQUEST;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        return (other instanceof SystemReportRequest);
    }

    @Override
    public String toString() {
        return "> SystemReportRequest";
    }
}
