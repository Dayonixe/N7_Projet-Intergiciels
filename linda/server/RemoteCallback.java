package linda.server;

import linda.Callback;
import linda.Tuple;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteCallback extends UnicastRemoteObject implements IRemoteCallback {
    private transient final Callback callback;
    public RemoteCallback(Callback callback) throws RemoteException {
        this.callback = callback;
    }

    @Override
    public void call(Tuple t) throws RemoteException {
        callback.call(t);
    }
}
