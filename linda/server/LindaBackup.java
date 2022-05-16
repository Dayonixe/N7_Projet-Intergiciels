package linda.server;

import linda.Callback;
import linda.Tuple;
import linda.shm.TupleCallback;
import linda.shm.TupleCallbackManager;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.Collectors;

public class LindaBackup implements Serializable {
    public final List<Tuple> tuples;
    public final List<TupleCallbackBackup> readCallbacks;
    public final List<TupleCallbackBackup> takeCallbacks;

    public LindaBackup(List<Tuple> tuples, TupleCallbackManager readCallbacks, TupleCallbackManager takeCallbacks) {
        this.tuples = tuples;
        this.readCallbacks = readCallbacks.getAll().stream()
                .map(TupleCallbackBackup::new).collect(Collectors.toList());
        System.out.println("readCallbacks "+readCallbacks);

        this.takeCallbacks = takeCallbacks.getAll().stream()
                .map(TupleCallbackBackup::new).collect(Collectors.toList());
    }

    public List<TupleCallback> toReadCallbacks() {
        return this.readCallbacks.stream().map(TupleCallbackBackup::toTupleCallback).collect(Collectors.toList());
    }

    public List<TupleCallback> toTakeCallbacks() {
        return this.takeCallbacks.stream().map(TupleCallbackBackup::toTupleCallback).collect(Collectors.toList());
    }

    public static class TupleCallbackBackup implements Serializable {
        public final Tuple template;
        public final IRemoteCallback callback;

        public TupleCallbackBackup(TupleCallback tupleCallback) {
            try {
                this.template = tupleCallback.template;
                this.callback = new RemoteCallback(tupleCallback.callback);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        public TupleCallback toTupleCallback() {
            return new TupleCallback(template, t -> {
                try {
                    callback.call(t);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
