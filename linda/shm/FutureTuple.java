package linda.shm;

import linda.Callback;
import linda.Tuple;

import java.util.concurrent.CompletableFuture;

public class FutureTuple {
    private final Tuple template;
    private final CompletableFuture<Tuple> future;

    /**
     *
     * @param template Template auquel le tuple doit correspondre
     * @param future Code à appeler une fois le tuple obtenu
     */
    public FutureTuple(Tuple template, CompletableFuture<Tuple> future) {
        this.template = template;
        this.future = future;
    }

    /**
     *
     * @param template Template auquel le tuple doit correspondre
     * @param callback Code à appeler une fois le tuple obtenu
     */
    public FutureTuple(Tuple template, Callback callback) {
        CompletableFuture<Tuple> future = new CompletableFuture<>();
        future.thenAccept(callback::call);
        this.template = template;
        this.future = future;
    }


    public boolean matches(Tuple t) {
        return t.matches(template);
    }

    public CompletableFuture<Tuple> future() {
        return future;
    }
}
