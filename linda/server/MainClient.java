package linda.server;

import linda.Tuple;

public class MainClient {
    public static void main(String[] args) {
        LindaClient client = new LindaClient("rmi://localhost:4000/LindaServer");
        client.write(new Tuple(5, 9));
        Tuple read = client.read(new Tuple(Integer.class, Integer.class));
        System.out.println("Read "+read);
    }
}
