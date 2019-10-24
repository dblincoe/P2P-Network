import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionManager {
    private ServerSocket transferSocket;
    
    public ConnectionManager(int port) throws IOException {
        transferSocket = new ServerSocket(port);
    }

    public void createNeighborConnections() {

    }

    public void closeNeighborConnections() {

    }

    public void get(String filename) {
        Query q = new Query(filename);
    }
}
