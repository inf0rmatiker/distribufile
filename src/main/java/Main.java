import chunkserver.Chunk;
import chunkserver.ChunkIntegrity;
import chunkserver.ChunkServer;
import client.FileClient;
import controller.Controller;
import messaging.ChunkStoreRequest;
import messaging.Message;
import messaging.MessageFactory;
import networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static util.Constants.CHUNK_SIZE;
import static util.Constants.KB;

public class Main {

    public static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        switch (args[0].trim()) {

            case "--chunkserver":

                if (args[1].contains("--controller=")) {
                    String controllerHostname = args[1].trim().replaceFirst("--controller=", "");
                    ChunkServer chunkServer = new ChunkServer(controllerHostname, Constants.CONTROLLER_PORT);
                    chunkServer.startServer();
                    chunkServer.startHeartbeatMinorTask();
                    chunkServer.startHeartbeatMajorTask();
                    break;
                } else {
                    log.warn("Usage: Main --chunkserver --controller=<hostname>");
                    System.exit(1);
                }

            case "--controller":

                Controller controller = new Controller();
                controller.startServer();
                controller.startHeartbeatMonitor();
                break;

            case "--client":

                if (args[1].contains("--controller=")) {
                    String controllerHostname = args[1].trim().replaceFirst("--controller=", "");
                    FileClient client = new FileClient(controllerHostname, Constants.CONTROLLER_PORT);

                    if (args[2].contains("--read=")) {
                        String filename = args[2].replaceFirst("--read=", "");
                        try {
                            client.readFile(filename);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (args[2].contains("--write=")) {

                        String filename = args[2].replaceFirst("--write=", "");
                        try {
                            client.writeFile(filename);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }

    }

    // Only used for hacky testing. TODO: Remove
    public static void clientSendMsg(String filename) {
        String testFile = filename;
        List<String> testReplicationChunkServers = new ArrayList<>(Arrays.asList("swordfish", "sardine"));
        log.info("input file: {}", testFile);

        try {
            // Read raw chunk data from 35KB file
            FileInputStream fileInputStream = new FileInputStream(testFile);
            BufferedInputStream reader = new BufferedInputStream(fileInputStream, CHUNK_SIZE);
            byte[] chunkData = new byte[35 * KB];
            int bytesRead = reader.read(chunkData, 0, 35 * KB);
            log.info("bytes read: {}", bytesRead);
            reader.close();
            fileInputStream.close();

            // Create ChunkStoreRequest with 35KB chunk data
            Integer testSequence = 0;
            ChunkStoreRequest chunkStoreRequest = new ChunkStoreRequest(Host.getHostname(), Host.getIpAddress(), 0,
                    testReplicationChunkServers, testFile, testSequence, chunkData);
            log.info("Request:\n{}", chunkStoreRequest);

            Socket clientSocket = Client.sendMessage("sole", Constants.CHUNK_SERVER_PORT, chunkStoreRequest);

            // Wait for ChunkStoreResponse from forward recipient
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            Message response = MessageFactory.getInstance().createMessage(dataInputStream);
            log.info("Response:\n{}", response);

            // Close our open socket with chunk server; we are done talking with them
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
