package util;

import java.net.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HostTest {

    @Test
    public void testGetHostIpAddress() {
        String ipAddress = Host.getIpAddress();
        assertNotEquals("", ipAddress);
    }

    @Test
    public void testGetHostHostname() {
        String hostname = Host.getHostname();
        assertNotEquals("", hostname);
    }
}
