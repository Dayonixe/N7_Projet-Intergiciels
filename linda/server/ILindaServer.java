package linda.server;

import linda.Linda;
import linda.Tuple;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Serveur Linda accessible à distance
 */
public interface ILindaServer extends Remote {
    void write(Tuple t) throws RemoteException;

    Tuple take(Tuple template) throws RemoteException;

    Tuple read(Tuple template) throws RemoteException;

    Tuple tryTake(Tuple template) throws RemoteException;

    Tuple tryRead(Tuple template) throws RemoteException;

    Collection<Tuple> takeAll(Tuple template) throws RemoteException;

    Collection<Tuple> readAll(Tuple template) throws RemoteException;

    void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, IRemoteCallback callback) throws RemoteException;

    /**
     * Donne l'adresse du serveur de backup ou null si aucun n'a été défini.
     * @return
     * @throws RemoteException
     */
    String getBackupAddress() throws RemoteException;

    /**
     * Recevoir le backup du serveur principal pour mettre à jour son état
     * @param backup
     * @throws RemoteException
     */
    void receiveBackup(LindaServerState backup) throws RemoteException;

    /**
     * Termine proprement le serveur Linda (avec sauvegarde de ses tuples).
     * @throws RemoteException
     */
    void shutdown() throws RemoteException;
}
