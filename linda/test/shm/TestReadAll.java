package linda.test.shm;

import linda.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TestReadAll extends TestSHM {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        Tuple template = new Tuple(Integer.class, Integer.class);

        writeAfter(0, new Tuple(3, "Quatre"));
        writeAfter(250, new Tuple(3, 6));
        writeAfter(0, new Tuple(9, 2));
        Thread.sleep(300);
        Collection<Tuple> tuples2 = LINDA.readAll(template);

        System.out.println("Written");

        Collection<Tuple> tuples = LINDA.readAll(template);

        assertEquals(2, tuples.size());
        assertEquals(2, tuples2.size());
    }
}
