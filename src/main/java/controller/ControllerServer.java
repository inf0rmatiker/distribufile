package controller;

import networking.Processor;
import networking.Server;
import util.Constants;

import java.net.Socket;

public class ControllerServer extends Server {

    public Controller controller;

    public ControllerServer(Controller controller) {
        this.controller = controller;
        this.bindToPort(Constants.CONTROLLER_PORT);
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public void processConnection(Socket clientSocket) {
        Processor processor = new ControllerProcessor(clientSocket, getController());
        processor.launchAsThread();
    }
}
