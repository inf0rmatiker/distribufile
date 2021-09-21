import chunkserver.Chunk;
import chunkserver.ChunkIntegrity;
import chunkserver.ChunkServer;
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


        if (args[0].equals("--chunkserver")) {

            // Start ChunkServer
            ChunkServer chunkServer = new ChunkServer("localhost", Constants.CONTROLLER_PORT);
            chunkServer.startServer();

        } else {
            String testFile = args[0];
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
}
