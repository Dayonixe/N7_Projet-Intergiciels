package linda.server;

import linda.Tuple;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Callback accessible à distance pour le LindaServer.
 * @see linda.server.LindaServer
 */
public interface IRemoteCallback extends Remote {
    void call(Tuple t) throws RemoteException;
}
