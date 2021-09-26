package controller;

import messaging.Message;
import networking.Client;

public class ControllerClient extends Client {

    public Controller controller;

    public ControllerClient(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }

}
