package linda.server;

import linda.Linda;
import linda.Tuple;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainServer {
    /**
     * Starts the backup Linda server and then the main server
     */
    public static void main(String[] args) throws Exception {
        Registry dns = LocateRegistry.createRegistry(4000);

        ILindaServer backup = new LindaServer(new File("backup.bin"));
        dns.bind("LindaBackup", backup);

        ILindaServer linda = new LindaServer(new File("linda_data.bin"), "rmi://localhost:4000/LindaBackup");
        dns.bind("LindaServer", linda);
    }
}
