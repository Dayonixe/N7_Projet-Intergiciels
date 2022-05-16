package linda.server;

import linda.Tuple;

import java.rmi.RemoteException;

public interface IRemoteCallback {
    void call(Tuple t) throws RemoteException;
}
