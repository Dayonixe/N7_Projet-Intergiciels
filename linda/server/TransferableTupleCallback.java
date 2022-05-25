package linda.server;

import linda.Tuple;
import linda.shm.TupleCallback;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Version de TupleCallback transferable à distance.
 * @see linda.shm.TupleCallback
 */
public class TransferableTupleCallback implements Serializable {
    private final Tuple template;
    private final IRemoteCallback callback;

    public TransferableTupleCallback(Tuple template, IRemoteCallback callback) {
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
