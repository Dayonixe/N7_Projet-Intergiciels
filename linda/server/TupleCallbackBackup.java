package linda.server;

import linda.Callback;
import linda.Tuple;
import linda.shm.TupleCallback;

import java.io.Serializable;
import java.rmi.RemoteException;

public class TupleCallbackBackup implements Serializable {
    private final Tuple template;
    private final IRemoteCallback callback;

    public TupleCallbackBackup(Tuple template, IRemoteCallback callback) {
        this.template = template;
        this.callback = callback;
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
