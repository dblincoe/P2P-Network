import java.io.IOException;
import java.net.ServerSocket;

public class TransferManager {
    private ServerSocket transferSocket;

    public TransferManager(int port) throws IOException {
        transferSocket = new ServerSocket(port);
    }
}
