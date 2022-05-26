package linda.test.server;

import linda.Tuple;
import linda.server.IRemoteCallback;
import linda.server.LindaServerState;
import linda.server.RemoteCallback;
import linda.server.TransferableTupleCallback;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class TestRemoteCallbackSerializable {
    @Test
    public void test() throws IOException {
        IRemoteCallback cb = new RemoteCallback(System.out::println);
        TransferableTupleCallback transferable = new TransferableTupleCallback(new Tuple(Integer.class, Integer.class), cb);
        LindaServerState state = new LindaServerState(new ArrayList<>());
        state.readers().add(transferable);
        new ObjectOutputStream(new FileOutputStream("out.test")).writeObject(state);
    }
}
