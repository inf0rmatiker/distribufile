package util;

import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HostTest {

    @Test
    public void testGetHostIpAddress() {
        try {
            String ipAddress = Host.getIpAddress();
            System.out.println(ipAddress); // for debugging
            assertNotEquals("", ipAddress);
        } catch (UnknownHostException e) {
            fail("Caught UnknownHostException!");
        }
    }

    @Test
    public void testGetHostHostname() {
        try {
            String hostname = Host.getHostname();
            System.out.println(hostname); // for debugging
            assertNotEquals("", hostname);
        } catch (UnknownHostException e) {
            fail("Caught UnknownHostException!");
        }


    }
}
