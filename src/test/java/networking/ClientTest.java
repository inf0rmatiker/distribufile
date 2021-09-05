package networking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.*;

public class ClientTest {

    public static Integer TESTING_PORT = 11218;
    public static String TESTING_HOST = "localhost";

    public static Integer DEFAULT_SERVER_PORT = 9001;
    public static String DEFAULT_SERVER_HOST = "localhost";

    public static Server server;
    public static Thread serverThread;

    @BeforeAll
    public static void setup() {
        server = new Server(DEFAULT_SERVER_PORT);
        serverThread = new Thread(server);
        serverThread.start();
    }

    @Test
    public void testDefaultConstructor() {
        Client client = new Client();
        assertEquals(client.getHost(), DEFAULT_SERVER_HOST);
        assertEquals(client.getPort(), DEFAULT_SERVER_PORT);
        try {
            client.getSocket().close();
            System.out.println("... client teardown\n");
        } catch (IOException e) {
            System.out.println("Could not close socket");
        }
    }

    @Test
    public void testHostOnlyConstructor() {
        Client client = new Client(TESTING_HOST);
        assertEquals(client.getHost(), TESTING_HOST);
        assertEquals(client.getPort(), DEFAULT_SERVER_PORT);
        try {
            client.getSocket().close();
            System.out.println("... client teardown\n");
        } catch (IOException e) {
            System.out.println("Could not close socket");
        }
    }

    // @Test
    // public void testArgConstructor() {
    //     Client client = new Client(TESTING_HOST, TESTING_PORT);
    //     assertEquals(client.getHost(), TESTING_HOST);
    //     assertEquals(client.getPort(), TESTING_PORT);
    //     try {
    //         client.getSocket().close();
    //         System.out.println("... client teardown\n");
    //     } catch (IOException e) {
    //         System.out.println("Could not close socket");
    //     }
    // }

    // @Test
    // public void testInitialConnection() {
    //     Client client = new Client();
    //     // System.setIn(new java.io.ByteArrayInputStream(TESTING_HOST.getBytes()));
    //     client.initialConnection();
    //     assert client.getSocket().isConnected();

    // }

    // public void tearDown(Client client) {
    //     try {
    //         client.getSocket().close();
    //         System.out.println("... client teardown\n");
    //     } catch (IOException e) {
    //         System.out.println("Could not close socket");
    //     }
    // }

    @AfterAll
    public static void tearDown() {
        try {
            serverThread.interrupt();
            server.getServerSocket().close();
            System.out.println("... server teardown\n");
        } catch (IOException e) {
            System.out.println("Could not close socket");
        } 
    }

}
