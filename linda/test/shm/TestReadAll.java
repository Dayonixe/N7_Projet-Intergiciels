package linda.test.shm;

import linda.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    @Test
    public void test2() {
        Tuple template = new Tuple(Integer.class, String.class);
        writeAfter(10, new Tuple(3, "Truc"));
        writeAfter(200, new Tuple(5, "TDuc"));
        CompletableFuture<Collection<Tuple>> readAll = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            LINDA.readAll(template); // should not remove tuples
            return LINDA.readAll(template);
        });

        List<Tuple> tuples = new ArrayList<>(readAll.join());

        assertEquals(new Tuple(3, "Truc"), tuples.get(0));
        assertEquals(new Tuple(5, "TDuc"), tuples.get(1));
    }
}
