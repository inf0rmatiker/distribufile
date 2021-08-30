package util;

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


}
