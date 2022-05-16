package linda.server;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainServer {
    public static void main(String[] args) throws Exception {
        ILindaServer linda = new LindaServer(new File("linda_data.bin"));
        Registry dns = LocateRegistry.createRegistry(4000);
        dns.bind("LindaServer", linda);
        System.out.println("Linda server started!");
    }
}
