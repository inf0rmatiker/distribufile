package networking;

import java.io.*;
import java.net.*;

public class Client {

    public static Integer SERVER_PORT = 9001;

    private Socket socket = null;

    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    public Client(String host, int port) {
        try {
            socket = new Socket(host, SERVER_PORT);
            
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            acceptUserInput();

            System.out.println("Client recieved back: " + dataInputStream.readUTF());

            dataInputStream.close();
            dataOutputStream.close();
            socket.close();

        } catch(UnknownHostException e) {
            System.out.println("Unknown Host Error: " + e.getMessage());
        } catch(IOException e) {
            System.out.println("IO Exception Error: " + e.getMessage());
        }
    }

    public void acceptUserInput() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String input = bufferedReader.readLine();
            dataOutputStream.writeUTF(input);
        } catch(IOException e) {
            System.out.println("IO Exception Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client("boston.cs.colostate.edu", SERVER_PORT);
    }

}
