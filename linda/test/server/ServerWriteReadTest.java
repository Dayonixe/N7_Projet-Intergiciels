package linda.test.server;

import linda.Tuple;
import linda.server.LindaClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

import static org.junit.Assert.assertEquals;

public class ServerWriteReadTest extends ServerTest {
    private static LindaClient client;

    @BeforeClass
    public static void setup() throws AlreadyBoundException, RemoteException {
        ServerTest.launchServer();
        client = new LindaClient("rmi://localhost:4000/LindaServer");
    }

    @Test
    public void testWriteRead() {
        client.write(new Tuple(5, 9));
        Tuple read = client.read(new Tuple(Integer.class, Integer.class));
        System.out.println("Read "+read);
        assertEquals(new Tuple(5, 9), read);
    }
}
