package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {
	private ILindaServer remote;
    private String backupServerAddress;

    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        connect(serverURI);
    }

    private void connect(String serverURI) {
        try {
            URI uri = new URI(serverURI);
            if(!(uri.getScheme() == null || uri.getScheme().equalsIgnoreCase("rmi"))) {
                throw new URISyntaxException(serverURI, "Invalid scheme. Expected rmi or nothing.");
            }
            Registry dns = LocateRegistry.getRegistry(uri.getHost(), uri.getPort());
            remote = (ILindaServer)dns.lookup(uri.getPath().substring(1));
            backupServerAddress = remote.getBackupAddress();
        } catch (RemoteException | NotBoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchToBackup() {
        if(backupServerAddress == null) {
            throw new RuntimeException("No backup server available.");
        }
        connect(backupServerAddress);
    }

    @Override
    public void write(Tuple t) {
        try {
            remote.write(t);
        } catch (RemoteException e) {
            switchToBackup();
            write(t);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return remote.take(template);
        } catch (RemoteException e) {
            switchToBackup();
            return take(template);
        }
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return remote.read(template);
        } catch (RemoteException e) {
            switchToBackup();
            return read(template);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return remote.tryTake(template);
        } catch (RemoteException e) {
            switchToBackup();
            return tryTake(template);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return remote.tryRead(template);
        } catch (RemoteException e) {
            switchToBackup();
            return tryRead(template);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return remote.takeAll(template);
        } catch (RemoteException e) {
            switchToBackup();
            return takeAll(template);
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return remote.readAll(template);
        } catch (RemoteException e) {
            switchToBackup();
            return readAll(template);
        }
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        try {
            IRemoteCallback remoteCallback = new RemoteCallback(callback);
            remote.eventRegister(mode, timing, template, remoteCallback);
        } catch (RemoteException e) {
            switchToBackup();
            eventRegister(mode, timing, template, callback);
        }
    }

    @Override
    public void debug(String prefix) {

    }
}
