package util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {

    /**
     * Retrieves the host's IP Address
     * @return The IP Address of the host, as a String
     */
    public static String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "UnknownIpAddress";
        }
    }

    /**
     * Retrieves the host's Hostname
     * @return The Hostname of the host, as a String
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UnknownHost";
        }
    }


}
