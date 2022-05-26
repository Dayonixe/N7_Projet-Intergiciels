package linda.test.shm;

import linda.Tuple;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTakeReadAll extends TestSHM {
    @Test
    public void testReadAllDuplicates() {
        Tuple template = new Tuple(Integer.class, Integer.class);
        LINDA.write(new Tuple(10, 10));
        LINDA.write(new Tuple(10, 10));
        LINDA.write(new Tuple(11, 16));
        LINDA.write(new Tuple(11, "non"));

        assertEquals(3, LINDA.readAll(template).size());
        assertEquals(3, LINDA.readAll(template).size());
    }

    @Test
    public void testTakeAllDuplicates() {
        Tuple template = new Tuple(Integer.class, Integer.class);
        LINDA.write(new Tuple(10, 10));
        LINDA.write(new Tuple(10, 10));
        LINDA.write(new Tuple(11, 16));
        LINDA.write(new Tuple(11, "non"));

        assertEquals(3, LINDA.takeAll(template).size());
        assertEquals(0, LINDA.takeAll(template).size());
    }
}
