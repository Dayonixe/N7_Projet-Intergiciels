package linda.test.shm;

import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class TestReadBeforeTake extends TestSHM {
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
}
