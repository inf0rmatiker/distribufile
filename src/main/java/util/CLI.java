package util;

import gnu.getopt.Getopt;

public class CLI {




    public static String[] getHostAndPort(String[] args) {
        Getopt g = new Getopt("CLI.java", args, "h:p:");
        int c;
        String host = "localhost";
        String port = String.valueOf(Constants.CONTROLLER_PORT);
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

        if(host.equals("localhost") && port.equals(String.valueOf(Constants.CONTROLLER_PORT))) {
            System.out.println("Host and Port set to default. \"" + "localhost" + "\" " + Constants.CONTROLLER_PORT);
            System.out.println(Constants.CLI_CLIENT_HELP);
        }

        return new String[]{host, port};

    }

    public static String[] getServerPort(String[] args) {
        Getopt g = new Getopt("CLI.java", args, "p:");
        int c;
        String port = String.valueOf(Constants.CONTROLLER_PORT);
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'p':
                    port = g.getOptarg();
                    break;
                case '?':
                    break; // getopt() already printed an error
            }
        }

        if(port.equals(String.valueOf(Constants.CONTROLLER_PORT))) {
            System.out.println("Port set to default. \"" + Constants.CONTROLLER_PORT + "\"");
            System.out.println(Constants.CLI_SERVER_HELP);
        }

        return new String[]{port};
    }

}
