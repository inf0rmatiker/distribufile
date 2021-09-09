package networking;

import java.io.*;
import java.net.*;

import util.CLI;
import util.Constants;


public class Client implements Runnable {

    private static class ClientException extends Exception {
        public ClientException(String message) {
            super(message);
        }
    }


    private String host;
    private int port;
    private Socket socket;
    // Probably dont need
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    // --- Constructors ---

    public Client() {
        this(Constants.DEFAULT_SERVER_HOST, Constants.DEFAULT_SERVER_PORT);
    }

    public Client(String host) {
        this(host, Constants.DEFAULT_SERVER_PORT);
    }

    public Client(String host, Integer port) {
            this.host = host;
            this.port = port;
            connectToServer(host, String.valueOf(port));
    }

    // --- Getters ---

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }


    // --- Public start method ---
    /**
     * Starts the client and connects to the server. A while loop continues to
     * accept input from the user and send it to the server.
     * 
     * @Override
     */
    public void run() {
        while (socket != null && !socket.isClosed()) {
            try {
                acceptUserInput();
            } catch (ClientException e) {
                System.out.println(e.getMessage());
                break;
            }
        }

    }

    // --- Private client functionality methods ---

    /**
     * Accepts user input to be filtered provide more functionality to the client.
     * 
     * @throws ClientException throws an exception if the user is exiting the client
     */
    private void acceptUserInput() throws ClientException {
        BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter a command: ");
        try {
            filterInput(stdIn.readLine());
        } catch (IOException e) {
            System.out.println("Failed to read System.in: " + e.getMessage());
        }


    }

    /**
     * Filters the users input to determine what to do with it.
     * 
     * @param input The users input.
     * @throws ClientException Throws ClientException when the client is exiting the program
     */
    private void filterInput(String input) throws ClientException {
        if (input == null) {
            return;
        }
        if (input.equals("exit")) {
            System.out.println("Exiting...");
            closeConnection();
        } else if (input.startsWith("send")) {
            String message = input.substring(input.indexOf(" ") + 1);
            sendMessage(message);
        } else if (input.contains("server_connect")) {
            String[] args = input.substring(input.indexOf(" ") + 1).split(" ");
            String[] parsedInput = CLI.getHostAndPort(args);
            connectToServer(parsedInput[0], parsedInput[1]);
        } else {
            System.out.println("Invalid Command.");
            displayCommands();
        }
    }

    /**
     * Close the Client objects connection to the server. This includes closing the
     * socket and the data streams. As well as the socket itself.
     * 
     * @throws ClientException throws exception when closing the client
     */
    private void closeConnection() throws ClientException {
        try {
            socket.close();
            throw new ClientException("Connection closed.");
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Displays the commands that the client can use.
     */
    private void displayCommands() {
        System.out.println("\nAvailable commands: ");
        System.out.println(
                "server_connect -h [host] -p [port]: if zero arguments are given or flags not given. \"localhost\" and port 9001 set by default.");
        System.out.println("send <message>: send message to server");
        System.out.println("exit: close client");
    }

    /**
     * Client utility method to send a message to the server. Mainly meant for
     * testing purposes until message factory is implemented.
     * 
     * @param message The message to be sent.
     */
    private void sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Client utility method to connect to a server of a users choice. If the user
     * does not provide a host and port, or if the user provides only one argument,
     * the default host and port are used. The default host is localhost and the
     * default port is 9001.
     * 
     * @param host the host the user is connecting to
     * @param port the port number the user will connect to
     */
    private void connectToServer(String host, String port) {
        try {

            if(this.socket != null && !this.socket.isClosed()) {
                closeConnection();
            }

            this.socket = new Socket(host, Integer.parseInt(port));
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());

            System.out.println("🔌Connected to server");

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        } catch (ClientException e) {
            System.out.println();
        }
    }

    public static void main(String[] args) {

        if(args.length != 4){
            System.out.println(Constants.CLI_CLIENT_HELP);
            System.exit(1);
        }
        else {
            String[] parsedInput = CLI.getHostAndPort(args);
            Client client = new Client(parsedInput[0], Integer.parseInt(parsedInput[1]));
            client.run();
        }
    }

}
