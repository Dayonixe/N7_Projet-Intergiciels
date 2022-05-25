package linda.test.shm;

import linda.Linda;
import linda.Tuple;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class TestWriteCallback extends TestSHM {
    @Test
    public void testCallbackWrite1() throws InterruptedException {
        Tuple template = new Tuple(Integer.class, String.class);
        writeAfter(10, new Tuple(3, 5));
        writeAfter(10, new Tuple(2, "Test"));
        Thread.sleep(100);
        CounterCallback counterCallback = new CounterCallback(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, template);
        counterCallback.register();
        writeAfter(200, new Tuple(1, "Test"));
        CompletableFuture<Void> lastWrite = writeAfter(400, new Tuple(1, 2));

        lastWrite.join();

        assertEquals(2, counterCallback.getCount());
    }

    @Test
    public void testCallbackWrite2() throws InterruptedException {
        Tuple template = new Tuple(Integer.class, String.class);
        writeAfter(10, new Tuple(3, 5));
        writeAfter(10, new Tuple(2, "Test"));
        Thread.sleep(100);
        CounterCallback counterCallback = new CounterCallback(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, template);
        counterCallback.register();
        writeAfter(200, new Tuple(1, "Test"));
        CompletableFuture<Void> lastWrite = writeAfter(400, new Tuple(1, 2));

        lastWrite.join();

        assertEquals(2, counterCallback.getCount());
    }
}
