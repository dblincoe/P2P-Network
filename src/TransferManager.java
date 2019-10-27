import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class TransferManager extends Thread {
    private ServerSocket transferSocket;
    private boolean running;

    private HashMap<String, TransferServer> transfers;

    public TransferManager(int port) throws IOException {
        transferSocket = new ServerSocket(port);

        transfers = new HashMap<>();
    }

    @Override
    public void run()
    {
        running = true;

        try
        {
            while (running)
            {
                TransferServer ts = new TransferServer(this.transferSocket.accept());
                transfers.put(ts.getAddress(), ts);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void closeTransferConnections() throws IOException
    {
        for (TransferServer ts : transfers.values()) {
            ts.close();
        }
        transfers.clear();
    }

    public void exit() throws IOException
    {
        closeTransferConnections();
        transferSocket.close();
        running = false;
    }
}
