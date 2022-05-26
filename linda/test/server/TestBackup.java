package linda.test.server;

import linda.Linda;
import linda.server.ILindaServer;
import linda.server.LindaClient;
import linda.server.LindaServer;
import org.junit.Test;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static org.junit.Assert.assertEquals;

public class TestBackup {
    @Test
    public void testBackupAddress() throws RemoteException, AlreadyBoundException, NotBoundException {
        // Setup backup & server
        ILindaServer backup = new LindaServer(new File("backup.bin"));
        Registry dns = LocateRegistry.createRegistry(4000);
        dns.bind("LindaBackup", backup);

        ILindaServer linda = new LindaServer(new File("linda_data.bin"), "rmi://localhost:4000/LindaBackup");
        dns.bind("LindaServer", linda);


        ILindaServer remote = (ILindaServer)dns.lookup("LindaServer");

        // Test
        assertEquals("rmi://localhost:4000/LindaBackup", remote.getBackupAddress());
    }
}
