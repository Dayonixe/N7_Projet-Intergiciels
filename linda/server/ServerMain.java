package linda.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        ILindaServer linda = new LindaServer();
        Registry dns = LocateRegistry.createRegistry(1099);
        dns.bind("linda", linda);
    }
}
