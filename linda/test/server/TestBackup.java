package linda.test.server;

import linda.server.ILindaServer;
import linda.server.LindaServer;
import org.junit.Test;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestBackup {
    @Test
    public void testBackupIsWorking() throws RemoteException, AlreadyBoundException {
        // Setup backup & server
        ILindaServer backup = new LindaServer(new File("backup.bin"));
        Registry dns = LocateRegistry.createRegistry(4000);
        dns.bind("LindaBackup", backup);

        ILindaServer linda = new LindaServer(new File("linda_data.bin"), "rmi://localhost:4000/LindaBackup");
        dns.bind("LindaServer", linda);

        // Test

    }
}
