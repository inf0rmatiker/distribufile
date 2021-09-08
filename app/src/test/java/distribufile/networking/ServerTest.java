package distribufile.networking;

import java.io.*;
import org.junit.jupiter.api.Test;


public class ServerTest {

    public static Integer TESTING_PORT = 11218; 
    public static String TESTING_HOST = "localhost";

    @Test
    public void testDefaultConstructor() {
        Server server = new Server();
        try {
            server.getServerSocket().close();
        } catch (IOException e) {
            System.out.println("Could not close server socket");
        }
    }

    @Test
    public void testArgConstructor() {
        Server server = new Server(TESTING_PORT);
        try {
            server.getServerSocket().close();
        } catch (IOException e) {
            System.out.println("Could not close server socket");
        }
    }

    @Test
    public void testRun() {
        //TODO: implement this test
    }
    
}
