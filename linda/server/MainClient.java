package linda.server;

public class MainClient {
    public static void main(String[] args) {
        LindaClient client = new LindaClient("rmi://localhost:4000/LindaServer");

    }
}
