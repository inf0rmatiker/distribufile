import chunkserver.ChunkServer;
import client.FileClient;
import controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.IOException;
import java.util.Arrays;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class Main {

    public static Logger log = LoggerFactory.getLogger(Main.class);

    public static void printUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: Main [options]\n\n");
        sb.append("--chunkserver <controller hostname>\n");
        sb.append("\t start chunk server with <controller hostname> it should contact.\n\n");
        sb.append("--controller\n");
        sb.append("\t start controller for current machine.\n\n");
        sb.append("--client-write <controller hostname> <file>\n");
        sb.append("\t write <file> to controller with <controller hostname> it should contact.\n\n");
        sb.append("--client-read <controller hostname> <file> <output path>\n");
        sb.append("\t read <file> from controller with <controller hostname> it should contact\n");
        sb.append("\t along with the output path of the file.\n\n");
        sb.append("--client-report <controller hostname>\n");
        sb.append("\t get controller's system report on all tracked files\n\n");
        System.out.println(sb);
    }

    public static LongOpt[] generateValidOptions() {
        LongOpt[] longopts = new LongOpt[5];
        longopts[0] = new LongOpt("chunkserver", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longopts[1] = new LongOpt("client-read", LongOpt.REQUIRED_ARGUMENT, null, 'r');
        longopts[2] = new LongOpt("client-write", LongOpt.REQUIRED_ARGUMENT, null, 'w');
        longopts[3] = new LongOpt("client-report", LongOpt.REQUIRED_ARGUMENT, null, 'p');
        longopts[4] = new LongOpt("controller", LongOpt.NO_ARGUMENT, null, 'c');
        return longopts;
    }

    public static void startChunkServer(String controllerHostname) {
        ChunkServer chunkServer = new ChunkServer(controllerHostname, Constants.CONTROLLER_PORT);
        chunkServer.startServer();
        chunkServer.startHeartbeatMinorTask();
        chunkServer.startHeartbeatMajorTask();
    }

    public static void startController() {
        Controller controller = new Controller();
        controller.startServer();
        controller.startHeartbeatMonitor();
    }

    public static String[] getReadWriteArgs(Getopt g, String[] args, boolean isRead) {
        int WRITE_ARGS_LENGTH = 2;
        int READ_ARGS_LENGTH = 3;
        String[] writeArgs = new String[isRead ? READ_ARGS_LENGTH : WRITE_ARGS_LENGTH];
        int index = g.getOptind() - 1;
        for (int i = 0; index < args.length; i++) {
                writeArgs[i] = args[index];
                index++;
        }
        g.setOptind(index - 1);
        return writeArgs;
    }

    public static void clientWriteFile(String controllerHostname, String filename) {
        FileClient client = new FileClient(controllerHostname, Constants.CONTROLLER_PORT);
        try {
            client.writeFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error writing file: {}", filename);
        }
    }

    public static void clientGetReport(String controllerHostname) {
        FileClient client = new FileClient(controllerHostname, Constants.CONTROLLER_PORT);
        try {
            client.getSystemReport();
        } catch (IOException e) {
            log.error("Error getting system report");
            e.printStackTrace();
        }
    }

    public static void clientReadFile(String controllerHostname, String filename, String output) {
        FileClient client = new FileClient(controllerHostname, Constants.CONTROLLER_PORT);
        try {
            client.readFile(filename, output);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error reading file: {} to output {}", filename, output);
        }
    }

    public static void main(String[] args) {
        Getopt g = new Getopt("Main.java", args, "", generateValidOptions(), true);
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 's':
                    startChunkServer(g.getOptarg());
                    break;
                case 'r':
                    String[] readArgs = getReadWriteArgs(g, args, true);
                    clientReadFile(readArgs[0], readArgs[1], readArgs[2]);
                    break;
                case 'w':
                    String[] writeArgs = getReadWriteArgs(g, args, false);
                    clientWriteFile(writeArgs[0], writeArgs[1]);
                    break;
                case 'p':
                    clientGetReport(g.getOptarg());
                    break;
                case 'c':
                    startController();
                    break;
                default:
                    printUsage();
                    System.exit(1);
            }
        }

    }

}
