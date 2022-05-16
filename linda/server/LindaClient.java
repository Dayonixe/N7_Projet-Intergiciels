package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

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
	private final ILindaServer remote;

    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        Registry dns;
        try {
            URI uri = new URI(serverURI);
            dns = LocateRegistry.getRegistry(uri.getHost(), uri.getPort());
            remote = (ILindaServer)dns.lookup(uri.getPath());
        } catch (RemoteException | NotBoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Tuple t) {
        try {
            remote.write(t);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return remote.take(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return remote.read(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return remote.tryTake(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return remote.tryRead(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return remote.takeAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return remote.readAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        try {
            RemoteCallback remoteCallback = new RemoteCallback(callback);
            remote.eventRegister(mode, timing, template, remoteCallback);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void debug(String prefix) {

    }
}
