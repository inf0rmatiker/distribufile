package util;

import gnu.getopt.Getopt;

public class CLI {




    public static String[] getHostAndPort(String[] args) {
        Getopt g = new Getopt("CLI.java", args, "h:p:");
        int c;
        String host = Constants.DEFAULT_SERVER_HOST;
        String port = String.valueOf(Constants.DEFAULT_SERVER_PORT);
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

        if(host.equals(Constants.DEFAULT_SERVER_HOST) && port.equals(String.valueOf(Constants.DEFAULT_SERVER_PORT))) {
            System.out.println("Host and Port set to default. \"" + Constants.DEFAULT_SERVER_HOST + "\" " + Constants.DEFAULT_SERVER_PORT);
            System.out.println(Constants.CLI_CLIENT_HELP);
        }

        return new String[]{host, port};

    }

    public static String[] getServerPort(String[] args) {
        Getopt g = new Getopt("CLI.java", args, "p:");
        int c;
        String port = String.valueOf(Constants.DEFAULT_SERVER_PORT);
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'p':
                    port = g.getOptarg();
                    break;
                case '?':
                    break; // getopt() already printed an error
            }
        }

        if(port.equals(String.valueOf(Constants.DEFAULT_SERVER_PORT))) {
            System.out.println("Port set to default. \"" + Constants.DEFAULT_SERVER_PORT + "\"");
            System.out.println(Constants.CLI_SERVER_HELP);
        }

        return new String[]{port};
    }

}
