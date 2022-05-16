package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LindaServer extends UnicastRemoteObject implements ILindaServer {
    private final File saveFile;
    private final CentralizedLinda linda;
    public LindaServer(File saveFile) throws RemoteException {
        this.saveFile = saveFile;
        List<Tuple> tuples = new ArrayList<>();
        if(this.saveFile.exists()) {
            tuples = load();
        }
        this.linda = new CentralizedLinda(tuples);
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
    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, RemoteCallback remoteCallback) throws RemoteException {
        Callback callback = t -> {
            try {
                remoteCallback.call(t);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        };
        linda.eventRegister(mode, timing, template, callback);
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
        if(!saveFile.exists()) {
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
        System.err.println("Invalid save file.");
    }

    public void shutdown() {
        save();
    }

}
