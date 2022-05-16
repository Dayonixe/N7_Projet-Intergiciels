package linda.server;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainServer {
    public static void main(String[] args) throws Exception {
        ILindaServer backup = new LindaServer(new File("backup.bin"));
        Registry dns = LocateRegistry.createRegistry(4000);
        dns.bind("LindaBackup", backup);

        ILindaServer linda = new LindaServer(new File("linda_data.bin"), "rmi://localhost:4000/LindaBackup");
        dns.bind("LindaServer", linda);
    }
}
