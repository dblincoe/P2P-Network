import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class TransferManager extends Thread {
    private ServerSocket transferSocket;
    private boolean running;

    private HashMap<String, TransferServer> transfers;

    TransferManager(int port) throws IOException {
        System.out.println("Transfer Port " + port);
        transferSocket = new ServerSocket(port);
        transfers = new HashMap<>();
    }

    // Accepts new connections for transfer
    @Override
    public void run() {
        running = true;
        try {
            while (running)  {
                TransferServer ts = new TransferServer(this.transferSocket.accept());
                System.out.println("Transfer connection accepted for " + ts.getAddress());
                ts.start();
                transfers.put(ts.getAddress(), ts);
            }
        } catch (IOException ignored) {
        }
    }

    // Close out of all connections
    private void closeTransferConnections() throws IOException {
        for (TransferServer ts : transfers.values()) {
            ts.close();
        }
        transfers.clear();
    }

    // Stops socket
    void exit() throws IOException {
        closeTransferConnections();
        transferSocket.close();
        running = false;
    }
}
