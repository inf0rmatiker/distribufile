package distribufile.util;


import distribufile.util.CLI;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CLITest {

    public static String DEFAULT_PORT = "9001";
    public static String DEFAULT_HOST = "localhost";
    public static String[] DEFAULT_ARGS = {"-p", DEFAULT_PORT.toString(), "-h", DEFAULT_HOST};

    @Test
    public void testGetHostAndPort() {
        String[] args = DEFAULT_ARGS;
        String[] result = CLI.getHostAndPort(args);
        assertEquals(DEFAULT_HOST, result[0]);
        assertEquals(DEFAULT_PORT, result[1]);
    }

    @Test
    public void testGetHostAndPortNoArgs() {
        String[] args = {};
        String[] result = CLI.getHostAndPort(args);
        assertEquals(DEFAULT_HOST, result[0]);
        assertEquals(DEFAULT_PORT, result[1]);
    }

    @Test
    public void testGetServerPort() {
        String[] args = {"-p", "8080"};
        String result = CLI.getServerPort(args)[0];
        assertEquals("8080", result);
    }

    @Test
    public void testGetServerPortNoArgs() {
        String[] args = {};
        String result = CLI.getServerPort(args)[0];
        assertEquals(DEFAULT_PORT, result);
    }
    
}
