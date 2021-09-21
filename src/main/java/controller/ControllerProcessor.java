package controller;

import messaging.HeartbeatMajor;
import messaging.HeartbeatMinor;
import messaging.Message;
import networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class ControllerProcessor extends Processor {

    public static Logger log = LoggerFactory.getLogger(ControllerProcessor.class);

    public Controller controller;

    public ControllerProcessor(Socket socket, Controller controller) {
        this.controller = controller;
        this.socket = socket;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public void processRequest(Message message) {
        log.info("Processing message for ControllerProcessor");

        switch (message.getType()) {
            case CLIENT_WRITE_REQUEST:
                log.info("Received chunk store request");
                break;
            case HEARTBEAT_MAJOR:
                log.info("Received major heartbeat");
                controller.processHeartbeatMajor((HeartbeatMajor) message);
                break;
            case HEARTBEAT_MINOR:
                log.info("Received minor heartbeat");
                controller.processHeartbeatMinor((HeartbeatMinor) message);
                break;
            default:
                log.error("Invalid Message type");
        }
    }
}
