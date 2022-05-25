package linda.server;

import linda.Tuple;
import linda.shm.TupleCallbackManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Etat du serveur linda transferable au backup.
 */
public class LindaServerState implements Serializable {
    private final List<TransferableTupleCallback> readers = new ArrayList<>();
    private final List<TransferableTupleCallback> takers = new ArrayList<>();
    private final List<Tuple> tuples;

    public LindaServerState(List<Tuple> tuples) {
        this.tuples = tuples;
    }

    public List<Tuple> tuples() {
        return tuples;
    }

    public List<TransferableTupleCallback> readers() {
        return readers;
    }

    public List<TransferableTupleCallback> takers() {
        return takers;
    }

    public TupleCallbackManager readersManager() {
        return new TupleCallbackManager(readers().stream().map(TransferableTupleCallback::toTupleCallback).collect(Collectors.toList()));
    }

    public TupleCallbackManager takersManager() {
        return new TupleCallbackManager(takers().stream().map(TransferableTupleCallback::toTupleCallback).collect(Collectors.toList()));
    }
}
