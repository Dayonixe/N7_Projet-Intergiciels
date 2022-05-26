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
    private final LindaServerState state;

    public LindaServer(File saveFile) throws RemoteException {
        this(saveFile, null);
    }

    public LindaServer(File saveFile, String backupURI) throws RemoteException {
        this.saveFile = saveFile;
        this.backupURI = backupURI;

        List<Tuple> tuples = new ArrayList<>();
        if (this.saveFile != null && this.saveFile.exists()) {
            System.out.println("Recovering from save file.");
            tuples = load();
        }

        this.state = new LindaServerState(tuples);
        this.linda = new CentralizedLinda(tuples);

        if (this.backupURI != null) {
            try {
                connectBackup();
                scheduleBackup();
                System.out.println("Linda server started with backup " + backupURI + ".");
            }catch (Exception e) {
                e.printStackTrace();
                System.err.println("Could not connect to backup. Ignoring it.");
            }
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
        List<TransferableTupleCallback> backups = mode == Linda.eventMode.TAKE ? state.takers() : state.readers();
        TransferableTupleCallback callbackup = new TransferableTupleCallback(template, remoteCallback);
        synchronized (backups) {
            Callback callback = t -> {
                try {
                    remoteCallback.call(t);
                    synchronized (backups) {
                        backups.remove(callbackup);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            };
            backups.add(callbackup);
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
        if(saveFile == null) {
            return;
        }
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create save file.");
                throw new RuntimeException(e);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            // On ne sauvegarde que les tuples car l'intérêt de sauvegarder les callbacks n'est pas toujours justifié
            oos.writeObject(linda.getTuples());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws RemoteException {
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
                backupServer.receiveBackup(state);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void receiveBackup(LindaServerState state) throws RemoteException {
        System.out.println("Received state : " + state);
        System.out.println("with " + state.readers().size() + " read callbacks.");
        this.linda = new CentralizedLinda(state.tuples(), state.readersManager(), state.takersManager());
    }

    private void connectBackup() throws URISyntaxException, RemoteException, NotBoundException {
        URI uri = new URI(backupURI);
        if (!(uri.getScheme() == null || uri.getScheme().equalsIgnoreCase("rmi"))) {
            throw new URISyntaxException(backupURI, "Invalid scheme. Expected rmi or nothing.");
        }
        Registry dns = LocateRegistry.getRegistry(uri.getHost(), uri.getPort());
        backupServer = (ILindaServer) dns.lookup(uri.getPath().substring(1));
    }
}
