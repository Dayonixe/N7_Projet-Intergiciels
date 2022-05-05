package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

public class EventListener {
    private final Tuple template;
    private final Linda.eventMode mode;
    private final Linda.eventTiming timing;
    private final Callback callback;

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
}
