package linda.test.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.server.LindaClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantLock;

public class ServerCallbackTest extends ServerTest {
    private static LindaClient client;

    @BeforeClass
    public static void setup() throws AlreadyBoundException, RemoteException {
        ServerTest.launchServer();
        client = new LindaClient("rmi://localhost:4000/LindaServer");
    }

    @Test
    public void testCallback() {
        ReentrantLock lock = new ReentrantLock();
        Callback callback = t -> {
            System.out.println("Callback called with "+t);
            lock.unlock();
        };
        client.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(Integer.class, Integer.class), callback);
        client.write(new Tuple(5, 9));
        lock.lock();
        System.out.println("Success");
    }
}
