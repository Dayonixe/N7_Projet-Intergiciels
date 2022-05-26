package linda.server;

import linda.Linda;
import linda.Tuple;

public class MainClient {
    /**
     * Just a test Linda Client
     */
    public static void main(String[] args) throws InterruptedException {
        LindaClient client = new LindaClient("rmi://localhost:4000/LindaServer");
        client.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(Integer.class, Integer.class), t -> {
            System.out.println("Callback called with " + t);
        });
        Thread.sleep(10000);
        client.write(new Tuple(5, 9));
        Tuple read = client.read(new Tuple(Integer.class, Integer.class));
        System.out.println("Read " + read);
    }
}
