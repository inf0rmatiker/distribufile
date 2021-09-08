package distribufile.networking;

import java.io.IOException;


public class ClientTest {

    public static Integer TESTING_PORT = 11218;
    public static String TESTING_HOST = "localhost";

    public static Integer DEFAULT_SERVER_PORT = 9001;
    public static String DEFAULT_SERVER_HOST = "localhost";

    public static Server server;
    public static Thread serverThread;


    // @Test
    // public void testDefaultConstructor() {
    //     Client client = new Client();
    //     assertEquals(client.getHost(), DEFAULT_SERVER_HOST);
    //     try {
    //         client.getSocket().close();
    //         System.out.println("... client teardown\n");
    //     } catch (IOException e) {
    //         System.out.println("Could not close socket");
    //     }
    // }

    // @Test
    // public void testHostOnlyConstructor() {
    //     Client client = new Client(TESTING_HOST);
    //     assertEquals(client.getHost(), TESTING_HOST);
    //     try {
    //         client.getSocket().close();
    //         System.out.println("... client teardown\n");
    //     } catch (IOException e) {
    //         System.out.println("Could not close socket");
    //     }
    // }



}
