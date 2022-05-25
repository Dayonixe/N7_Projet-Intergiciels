package linda.server;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartServer {
    public static void main(String[] args) throws Exception {
        Registry dns = LocateRegistry.getRegistry(4000);

        ILindaServer linda = new LindaServer(new File("linda_data.bin"), "rmi://localhost:4000/LindaBackup");
        dns.bind("LindaServer", linda);
    }
}
