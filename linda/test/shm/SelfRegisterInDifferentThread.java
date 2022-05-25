package linda.test.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class SelfRegisterInDifferentThread extends TestSHM {
    @Test
    public void test() {
        Callback c1 = t -> System.out.println("c1");
        Callback c2 = t -> {
            CompletableFuture.runAsync(() -> {
                System.out.println("Register");
                LINDA.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(Integer.class, Integer.class), c1);
                System.out.println("after");
            }).join();
        };

        LINDA.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(Integer.class, Integer.class), c2);
        CompletableFuture<Void> write = writeAfter(10, new Tuple(9, 9));
        write.join();
    }
}
