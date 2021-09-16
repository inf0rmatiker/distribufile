package controller;


public class Controller {

    public Controller() {}

    public void startServer() {
        new ControllerServer(this).launchAsThread();
    }

    public void updateFreeSpaceAvailable() {

    }
}
