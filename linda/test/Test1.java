package linda.test;

import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Test1 {
    private static final Linda LINDA = new CentralizedLinda();

    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    @Test
    public void test() throws InterruptedException, ExecutionException {
        Tuple template = new Tuple(Integer.class, String.class);

        Tuple theTuple = new Tuple(3, "Hello");

        CompletableFuture<Tuple> taker = take(template);
        CompletableFuture<Tuple> reader1 = read(template);
        CompletableFuture<Tuple> reader2 = read(template);
        CompletableFuture<Tuple> reader3 = read(template);

        writeAfter(100, new Tuple(2, 3));
        writeAfter(1000, theTuple);

        CompletableFuture.allOf(taker, reader1, reader2, reader3);

        assertEquals(theTuple, taker.get());
        assertEquals(theTuple, reader1.get());
        assertEquals(theTuple, reader2.get());
        assertEquals(theTuple, reader3.get());

        Tuple tuple = LINDA.tryRead(template);
        assertNull(tuple);
    }

    private CompletableFuture<Tuple> read(Tuple template) {
        return CompletableFuture.supplyAsync(() -> {
            Tuple tuple = LINDA.read(template);
            System.out.println("Read tuple: "+tuple);
            return tuple;
        }, EXECUTOR);
    }

    private CompletableFuture<Tuple> take(Tuple template) {
        return CompletableFuture.supplyAsync(() -> {
            Tuple tuple = LINDA.take(template);
            System.out.println("Took tuple: "+tuple);
            return tuple;
        }, EXECUTOR);
    }

    private void writeAfter(long ms, Tuple tuple) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            LINDA.write(tuple);
        }, EXECUTOR);
    }
}
