package networking;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static Integer DEFAULT_SERVER_PORT = 9001;
    public static String DEFAULT_SERVER_HOST = "localhost";

    private Socket socket = null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    // --- Constructors ---

    public Client() {
        this(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
    }

    public Client(String host) {
        this(host, DEFAULT_SERVER_PORT);
    }

    public Client(String host, int port) {
        try {
            socket = new Socket(host, DEFAULT_SERVER_PORT);

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

        } catch (UnknownHostException e) {
            System.out.println("Unknown Host Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception Error: " + e.getMessage());
        }
    }

    // --- Public start method ---

    public void start() {
        initialConnection();
        while (!socket.isClosed()) {
            acceptUserInput();
        }
    }

    // --- Private client functionality methods ---

    /**
     * Initial connection to the server. The user can specify the host.
     */
    private void initialConnection() {
        System.out.println("Enter what host you want to connect to.");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        connectToServer(input);
    }

    /**
     * Accepts user input to be filtered provide more functionality to the client.
     */
    private void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);  

        System.out.print("Enter a command: ");
        String input = scanner.nextLine();

        filterInput(input);
    }

    /**
     * Filters the users input to determine what to do with it. 
     * @param input The users input.
     */
    private void filterInput(String input) {
        if (input.equals("exit")) {
            System.out.println("Exiting...");
            closeConnection();
        } else if (input.equals("help")) {
            displayCommands();
        } else if (input.startsWith("send")) {
            String message = input.substring(input.indexOf("send") + 1);
            sendMessage(message);
        } else if(input.contains("server_connect")){
            connectToServer(input);
        } else {
            System.out.println("Invalid command: " + input);
        }
    }

    /**
     * Close the Client objects connection to the server. This includes closing the socket and the data streams. 
     * As well as the socket itself.
     * @IOException If the socket is already closed.
     */
    private void closeConnection() {
        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
            System.out.println("Connection closed");
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    /** 
     * Displays the commands that the client can use.
     */
    private void displayCommands() {
        System.out.println("\nAvailable commands: ");
        System.out.println("server_connect <host port>: if zero or one argument given. \"localhost\" and port 9001 set by default.");
        System.out.println("send <message>: send message to server");
        System.out.println("exit: close client");
        System.out.println("help");
    }

    /**
     * Client utility method to send a message to the server. Mainly meant for testing purposes until message factory is implemented.
     * @param message The message to be sent.
     */
    private void sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }

    /** 
     * Client utility method to connect to a server of a users choice. If the user does not provide a host and port, or 
     * if the user provides only one argument, the default host and port are used. The default host is localhost and the default port is 9001.
     * @param input The users input. Contatining the host and port.
    */
    private void connectToServer(String input) {
        String[] args = input.split(" ");
        String host = args.length == 1 ? args[1] : DEFAULT_SERVER_HOST;
        Integer port = args.length == 1 ? Integer.parseInt(args[2]) : DEFAULT_SERVER_PORT;
        try {
             this.socket = new Socket(host, port);
             this.dataInputStream = new DataInputStream(socket.getInputStream());
             this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
             System.out.println("Connected to server");
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

}
