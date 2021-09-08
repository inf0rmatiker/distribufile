package gradle_project.util;

import gnu.getopt.Getopt;

public class CLI {

    public static Integer DEFAULT_PORT = 9001;
    public static String DEFAULT_HOST = "localhost";

    public static String CLI_CLIENT_HELP = "Run with command line inputs to change host and port:\n -h hostname \n -p port number";
    public static String CLIE_SERVER_HELP = "Run with command line inputs to change port: \n -p port number";

    public static String[] getHostAndPort(String[] args) {
        Getopt g = new Getopt("CLI.java", args, "h:p:");
        int c;
        String host = DEFAULT_HOST;
        String port = String.valueOf(DEFAULT_PORT);
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    host = g.getOptarg();
                    break;
                case 'p':
                    port = g.getOptarg();
                    break;
                case '?':
                    break; // getopt() already printed an error
            }
        }

        if(host.equals(DEFAULT_HOST) && port.equals(String.valueOf(DEFAULT_PORT))) {
            System.out.println("Host and Port set to default. \"" + DEFAULT_HOST + "\" " + DEFAULT_PORT);
            System.out.println(CLI_CLIENT_HELP);
        }

        return new String[]{host, port};

    }

    public static String[] getServerPort(String[] args) {
        Getopt g = new Getopt("CLI.java", args, "p:");
        int c;
        String port = String.valueOf(DEFAULT_PORT); 
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'p':
                    port = g.getOptarg();
                    break;
                case '?':
                    break; // getopt() already printed an error
            }
        }

        if(port.equals(String.valueOf(DEFAULT_PORT))) {
            System.out.println("Port set to default. \"" + DEFAULT_PORT + "\"");
            System.out.println(CLIE_SERVER_HELP);
        }

        return new String[]{port};
    }

}
