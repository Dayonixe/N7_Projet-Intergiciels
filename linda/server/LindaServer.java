package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class LindaServer extends UnicastRemoteObject implements ILindaServer {
    private final File saveFile;
    private CentralizedLinda linda;

    private final String backupURI;

    private ILindaServer backupServer;
    private final LindaBackup backup;

    public LindaServer(File saveFile) throws RemoteException {
        this(saveFile, null);
    }

    public LindaServer(File saveFile, String backupURI) throws RemoteException {
        this.saveFile = saveFile;
        List<Tuple> tuples = new ArrayList<>();
        if (this.saveFile.exists()) {
            tuples = load();
        }
        this.backup = new LindaBackup(tuples);
        this.linda = new CentralizedLinda(tuples);
        this.backupURI = backupURI;
        if (this.backupURI != null) {
            connectBackup();
            scheduleBackup();
            System.out.println("Linda server started with backup " + backupURI + ".");
        } else {
            System.out.println("Linda server started without backup.");
        }
    }

    @Override
    public void write(Tuple t) throws RemoteException {
        linda.write(t);
    }

    @Override
    public Tuple take(Tuple template) throws RemoteException {
        return linda.take(template);
    }

    @Override
    public Tuple read(Tuple template) throws RemoteException {
        return linda.read(template);
    }

    @Override
    public Tuple tryTake(Tuple template) throws RemoteException {
        return linda.tryTake(template);
    }

    @Override
    public Tuple tryRead(Tuple template) throws RemoteException {
        return linda.tryRead(template);
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException {
        return linda.takeAll(template);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) throws RemoteException {
        return linda.readAll(template);
    }

    @Override
    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, IRemoteCallback remoteCallback) throws RemoteException {
        List<TupleCallbackBackup> callbacks = mode == Linda.eventMode.TAKE ? backup.takers() : backup.readers();
        TupleCallbackBackup callbackup = new TupleCallbackBackup(template, remoteCallback);
        synchronized (callbacks) {
            Callback callback = t -> {
                try {
                    remoteCallback.call(t);
                    synchronized (callbacks) {
                        callbacks.remove(callbackup);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            };
            linda.eventRegister(mode, timing, template, callback);
        }
    }

    private List<Tuple> load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            return (List<Tuple>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.err.println("Invalid save file.");
        return new ArrayList<>();
    }

    public void save() {
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create save file.");
                throw new RuntimeException(e);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(linda.getTuples());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        save();
    }

    @Override
    public String getBackupAddress() throws RemoteException {
        return backupURI;
    }

    private void scheduleBackup() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendBackup();
                save();
            }
        }, 0L, 5000L);
    }

    private void sendBackup() {
        if (backupServer != null) {
            try {
                // Pb : création remote callback côté serveur principal
                // Donc si le serveur est down les callbacks aussi
                backupServer.receiveBackup(backup);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void receiveBackup(LindaBackup backup) throws RemoteException {
        System.out.println("Received backup : " + backup);
        System.out.println("with " + backup.readers().size() + " read callbacks.");
        this.linda = new CentralizedLinda(backup.tuples(), backup.readersManager(), backup.takersManager());
    }

    private void connectBackup() {
        try {
            URI uri = new URI(backupURI);
            if (!(uri.getScheme() == null || uri.getScheme().equalsIgnoreCase("rmi"))) {
                throw new URISyntaxException(backupURI, "Invalid scheme. Expected rmi or nothing.");
            }
            Registry dns = LocateRegistry.getRegistry(uri.getHost(), uri.getPort());
            backupServer = (ILindaServer) dns.lookup(uri.getPath().substring(1));
        } catch (RemoteException | NotBoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
