package linda.test.server;

import linda.server.LindaServer;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public abstract class ServerTest {
    private static LindaServer linda;

    public static void launchServer() throws RemoteException, AlreadyBoundException {
        if(linda != null) {
            return;
        }
        linda = new LindaServer(null);
        Registry dns = LocateRegistry.createRegistry(4000);
        dns.bind("LindaServer", linda);
    }
}
