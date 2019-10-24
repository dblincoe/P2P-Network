import java.io.IOException;
import java.net.Socket;

public class ConnectionThread extends Thread
{
    private Socket socket;
    
    public ConnectionThread(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
    
    }
    
    public void close() throws IOException
    {
        socket.close();
    }

}
