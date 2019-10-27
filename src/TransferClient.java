import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class TransferClient extends Thread {

    Socket socket;
    Transfer tPacket;

    String fullFilePath;

    Timer transferTimer;

    public TransferClient(Response r) throws IOException {
        socket = new Socket(r.getIp(), r.getPort());
        tPacket = new Transfer(r.getFilename());

        fullFilePath = "./obtained/" + r.getFilename();

        transferTimer = new Timer();
    }

    @Override
    public void run() {
        try {
            byte[] mBytes = tPacket.toString().getBytes();
            socket.getOutputStream().write(mBytes);

            FileOutputStream output = new FileOutputStream(fullFilePath, true);

            transferTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        byte[] data = new byte[1024];
                        int bytesRead = socket.getInputStream().read(data);
                        while (bytesRead != -1) {
                            output.write(bytesRead);
                            bytesRead = socket.getInputStream().read(data);
                        }
                        output.close();
                        transferTimer.cancel();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
