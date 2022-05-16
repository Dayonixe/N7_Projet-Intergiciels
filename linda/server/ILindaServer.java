package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

interface ILindaServer extends Remote {
    void write(Tuple t) throws RemoteException;

    Tuple take(Tuple template) throws RemoteException;

    Tuple read(Tuple template) throws RemoteException;

    Tuple tryTake(Tuple template) throws RemoteException;

    Tuple tryRead(Tuple template) throws RemoteException;

    Collection<Tuple> takeAll(Tuple template) throws RemoteException;

    Collection<Tuple> readAll(Tuple template) throws RemoteException;

    void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, RemoteCallback callback) throws RemoteException;
}
