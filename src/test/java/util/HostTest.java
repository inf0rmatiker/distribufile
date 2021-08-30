package util;

import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.fail;

public class HostTest {

    @Test
    public void testGetHostIpAddress() {
        try {
            System.out.println(Host.getIpAddress());
        } catch (UnknownHostException e) {
            fail("Caught UnknownHostException!");
        }
    }
}
