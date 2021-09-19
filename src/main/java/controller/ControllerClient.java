package controller;

import messaging.Message;
import networking.Client;

public class ControllerClient extends Client implements Runnable {

    public Controller controller;

    public ControllerClient(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }


    @Override
    public void run() {
        // TODO: Wait for response, then process it
    }

    @Override
    public void processResponse(Message message) {
        // TODO: Process Message message
    }
}
