package linda.test.shm;

import linda.Linda;
import linda.Tuple;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestReadCallbackBeforeTake extends TestSHM {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        Tuple template = new Tuple(String.class, String.class);

        CompletableFuture<Tuple> take1 = take(template);

        CounterCallback counterCallback = new CounterCallback(Linda.eventMode.READ, Linda.eventTiming.FUTURE, template);
        counterCallback.register();

        CompletableFuture<Void> write1 = writeAfter(1000, new Tuple("Hello", "world"));
        CompletableFuture<Void> write2 = writeAfter(2000, new Tuple("Hello", "world"));

        System.out.println("Waiting for tuple...");
        CompletableFuture.allOf(take1, write1, write2).join();

        assertNotNull(take1.get());
        assertEquals(2, counterCallback.getCount());
    }
}
