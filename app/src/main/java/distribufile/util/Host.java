package distribufile.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {

    /**
     * Retrieves the host's IP Address
     * @return The IP Address of the host, as a String
     * @throws UnknownHostException
     */
    public static String getIpAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * Retrieves the host's Hostname
     * @return The Hostname of the host, as a String
     * @throws UnknownHostException
     */
    public static String getHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }


}
