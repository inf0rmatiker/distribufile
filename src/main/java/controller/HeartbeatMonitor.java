package controller;

import chunkserver.ChunkMetadata;
import messaging.ChunkReplicateCommand;
import networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;
import util.Host;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.TimerTask;

/**
 * Responsible for monitoring the heartbeats of Chunk Servers.
 * If a Chunk Server fails to send a heartbeat at its expected interval (plus some grace period),
 * it is considered dead handled accordingly.
 */
public class HeartbeatMonitor extends TimerTask {

    public static Logger log = LoggerFactory.getLogger(HeartbeatMonitor.class);

    public Controller controller;

    public HeartbeatMonitor(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public void run() {
        for (ChunkServerMetadata chunkServer: getController().getChunkServerMetadata().values()) {
            if (chunkServer.isExpired()) {
                log.warn("Chunk Server {} has failed to send any heartbeat messages within interval; expiring it",
                        chunkServer.getHostname());
                expireChunkServer(chunkServer);
            }
        }
    }

    /**
     * Invoked if no Heartbeats are received from a Chunk Server after a given grace period.
     * The information we had on it is used to replicate all chunks it hosted elsewhere, using the
     * other valid replicas of the chunks stored on other Chunk Servers.
     * Finally, it is removed as a host, not only from each of the chunk's host lists,
     * but also from the master list of ChunkServerMetadata the Controller maintains.
     * @param chunkServer ChunkServerMetadata of the dead Chunk Server
     */
    public void expireChunkServer(ChunkServerMetadata chunkServer) {

        // Figure out which chunks the dead Chunk Server was maintaining
        for (ChunkMetadata lostChunk: chunkServer.getChunkMetadata()) {
            String deadChunkServer = chunkServer.getHostname();
            String filename = lostChunk.getAbsoluteFilePath();
            Integer sequence = lostChunk.getSequence();
            log.info("Initiating replacement of chunk {}, sequence {}", filename, sequence);

            FileMetadata fileMetadata = getController().getFilesMetadata().get(filename);
            Set<String> hosts = fileMetadata.get(sequence);

            boolean foundFreeChunkServer = false;
            for (ChunkServerMetadata csm: getController().getChunkServerMetadata().values()) {
                if (!hosts.contains(csm.getHostname())) { // choose a Chunk Server which doesn't already host this chunk
                    foundFreeChunkServer = true;
                    hosts.remove(deadChunkServer); // remove dead Chunk Server from list of hosts
                    log.info("Remaining hosts for chunk {}, sequence {}: {}", filename, sequence, hosts);
                    String validReplicaHost = hosts.iterator().next();
                    String targetChunkServer = csm.getHostname();
                    ChunkReplicateCommand replicateCommand = new ChunkReplicateCommand(
                            Host.getHostname(),
                            Host.getIpAddress(),
                            Constants.CONTROLLER_PORT,
                            targetChunkServer,
                            filename,
                            sequence
                    );

                    // Send command to a free Chunk Server to replicate the lost chunk from the dead Chunk Server
                    try {
                        Socket clientSocket = Client.sendMessage(validReplicaHost, Constants.CHUNK_SERVER_PORT,
                                replicateCommand);
                        clientSocket.close(); // done talking to valid chunk replica host
                        log.info("Successfully sent ChunkReplicateCommand to {}", validReplicaHost);
                    } catch (IOException e) {
                        log.error("Unable to send ChunkReplicateCommand to {}: {}", validReplicaHost, e.getMessage());
                    }
                    break; // done with this chunk, move on to the next
                }
            }

            if (!foundFreeChunkServer) {
                log.error("Unable to find a free Chunk Server to store chunk {}, sequence {}", filename, sequence);
            }
        }

        // Done with redistributing chunks, now nuke the Chunk Server from ChunkServerMetadata set
        getController().getChunkServerMetadata().remove(chunkServer.getHostname());
        log.info("Successfully removed {} from tracked Chunk Servers", chunkServer.getHostname());
    }
}
