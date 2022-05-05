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

        LINDA.write(new Tuple(3, "Quatre"));
        LINDA.write(new Tuple(3, 1));
        LINDA.write(new Tuple(3, 6));
        LINDA.write(new Tuple("Deux", 1));

        CompletableFuture<Tuple> read = read(template);

        System.out.println("Written");

        CompletableFuture<Void> writer = CompletableFuture.runAsync(() -> {
            writeAfter(0, new Tuple(2, 9));
            writeAfter(0, new Tuple(2, 9));
            writeAfter(0, new Tuple(2, 9));
            writeAfter(0, new Tuple(2, 9));
            writeAfter(0, new Tuple(2, 9));
        });

        CompletableFuture<Collection<Tuple>> trucs = CompletableFuture.supplyAsync(() -> LINDA.readAll(template));

        CompletableFuture<Void> future = CompletableFuture.allOf(writer, trucs);

        Collection<Tuple> tuples = LINDA.readAll(template);
        Collection<Tuple> tuples2 = LINDA.readAll(template);

        System.out.println("trucs "+trucs.get());

        LINDA.write(new Tuple(3, 9));

        assertEquals(2, tuples.size());
        assertEquals(2, tuples2.size());

        read.join();
    }
}
