package networking;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static Integer SERVER_PORT = 9001;

    private Socket socket = null;

    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    public Client() {
        this("localhost", SERVER_PORT);
    }

    public Client(String host) {
        this(host, SERVER_PORT);
    }

    public Client(String host, int port) {
        try {
            socket = new Socket(host, SERVER_PORT);

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

        } catch (UnknownHostException e) {
            System.out.println("Unknown Host Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception Error: " + e.getMessage());
        }
    }

    public void start() {
        while (!socket.isClosed()) {
            acceptUserInput();
        }
    }

    private void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);  

        System.out.print("Enter a command: ");
        String input = scanner.nextLine();

        filterInput(input);
    }

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

    private void displayCommands() {
        System.out.println("\nAvailable commands: ");
        System.out.println("server_connect <host port>: if zero or one argument give. localhost and port 9001 by default.");
        System.out.println("send <message>: send message to server");
        System.out.println("exit: close client");
        System.out.println("help");
    }


    private void sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }

    private void connectToServer(String input) {
        String[] args = input.split(" ");
        String host = args.length == 1 ? args[1] : "localhost";
        Integer port = args.length == 1 ? Integer.parseInt(args[2]) : SERVER_PORT;
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
        System.out.println("Enter what host you want to connect to.");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        Client client = new Client(input);
        client.start();
    }

}
