package linda.server;

import linda.Callback;
import linda.Tuple;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteCallback extends UnicastRemoteObject implements IRemoteCallback {
    private final Callback toCall;
    public RemoteCallback(Callback toCall) throws RemoteException {
        this.toCall = toCall;
    }

    @Override
    public void call(Tuple t) throws RemoteException {
        toCall.call(t);
    }
}
