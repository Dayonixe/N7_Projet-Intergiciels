package linda.test.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(Parameterized.class)
public abstract class TestSHM {
    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[1][0]; //new Object[10][0]; pour lancer 10 fois chaque test (pour s'assurer qu'il passe tout le temps)
    }
    protected final Linda LINDA = new CentralizedLinda();
    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    protected CompletableFuture<Tuple> read(Tuple template) {
        return CompletableFuture.supplyAsync(() -> {
            Tuple tuple = LINDA.read(template);
            System.out.println("Read "+tuple);
            return tuple;
        }, EXECUTOR);
    }

    protected CompletableFuture<Tuple> take(Tuple template) {
        return CompletableFuture.supplyAsync(() -> {
            Tuple tuple = LINDA.take(template);
            System.out.println("Took "+tuple);
            return tuple;
        }, EXECUTOR);
    }

    protected CompletableFuture<Void> writeAfter(long ms, Tuple tuple) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Write "+tuple);
            LINDA.write(tuple);
        }, EXECUTOR);
    }

    class CounterCallback implements Callback {
        private final Linda.eventMode mode;
        private final Linda.eventTiming timing;
        private final Tuple template;
        private int count;

        public CounterCallback(Linda.eventMode mode, Linda.eventTiming timing, Tuple template) {
            this.mode = mode;
            this.timing = timing;
            this.template = template;
        }

        @Override
        public void call(Tuple t) {
            System.out.println("Callback "+t);
            count++;
            register();
        }

        public int getCount() {
            return count;
        }

        public void register() {
            System.out.println("Register callback");
            LINDA.eventRegister(mode, timing, template, this);
        }
    }
}
