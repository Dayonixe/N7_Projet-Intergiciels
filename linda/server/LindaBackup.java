package linda.server;

import linda.Callback;
import linda.Tuple;
import linda.shm.TupleCallbackManager;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LindaBackup implements Serializable {
    private final List<TupleCallbackBackup> readers = new ArrayList<>();
    private final List<TupleCallbackBackup> takers = new ArrayList<>();
    private final List<Tuple> tuples;

    public LindaBackup(List<Tuple> tuples) {
        this.tuples = tuples;
    }

    public List<Tuple> tuples() {
        return tuples;
    }

    public List<TupleCallbackBackup> readers() {
        return readers;
    }

    public List<TupleCallbackBackup> takers() {
        return takers;
    }

    public TupleCallbackManager readersManager() {
        return new TupleCallbackManager(readers().stream().map(TupleCallbackBackup::toTupleCallback).collect(Collectors.toList()));
    }

    public TupleCallbackManager takersManager() {
        return new TupleCallbackManager(takers().stream().map(TupleCallbackBackup::toTupleCallback).collect(Collectors.toList()));
    }
}
