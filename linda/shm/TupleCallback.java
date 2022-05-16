package linda.shm;

import linda.Callback;
import linda.Tuple;

import java.util.concurrent.CompletableFuture;

public class TupleCallback extends CompletableFuture<Tuple> {
    private final Tuple template;

    /**
     *
     * @param template Template auquel le tuple doit correspondre
     * @param callback Code à appeler une fois le tuple obtenu
     */
    public TupleCallback(Tuple template, Callback callback) {
        this.thenAccept(callback::call);
        this.template = template;
    }

    public boolean matches(Tuple t) {
        return t.matches(template);
    }
}