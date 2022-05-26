package linda.test.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.server.LindaClient;
import org.junit.Test;

public class TestBackupServer {
    @Test
    public void testCallback() throws InterruptedException {
        /**
         * PLAN :
         * Lancer linda.server.StartBackup
         * Lancer linda.server.StartServer
         * Lorsque "kill main" est affiché : tuer le serveur principal (StartServer)
         * Vérifier que le callback est quand-même appelé après
         */
        Callback cb = t -> System.out.println("Got "+t);
        LindaClient client = new LindaClient("rmi://localhost:4000/LindaServer");
        client.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(Integer.class, Integer.class), cb);
        System.out.println(client.readAll(new Tuple(Integer.class, Integer.class)));
        Thread.sleep(5000);
        System.out.println("kill main");
        Thread.sleep(5000);
        client.write(new Tuple(10, 11));
        Thread.sleep(1000);
    }

}
