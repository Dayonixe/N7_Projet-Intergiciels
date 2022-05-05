package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/**
 * Sorted by creation time
 */
public class EventListener implements Comparable<EventListener> {
    private final Tuple template;
    private final Linda.eventMode mode;
    private final Linda.eventTiming timing;
    private final Callback callback;

    private final long createTime = System.currentTimeMillis();

    public EventListener(Tuple template, Linda.eventMode mode, Linda.eventTiming timing, Callback callback) {
        this.template = template;
        this.mode = mode;
        this.timing = timing;
        this.callback = callback;
    }

    public Tuple getTemplate() {
        return template;
    }

    public Linda.eventMode getMode() {
        return mode;
    }

    public Linda.eventTiming getTiming() {
        return timing;
    }

    public Callback getCallback() {
        return callback;
    }

    public void call(Tuple tuple) {
        getCallback().call(tuple);
    }

    public boolean tryCall(Tuple tuple) {
        if (!tuple.matches(template)) {
            return false;
        }
        getCallback().call(tuple);
        return true;
    }

    @Override
    public int compareTo(EventListener o) {
        return (int) (createTime - o.createTime);
    }
}
