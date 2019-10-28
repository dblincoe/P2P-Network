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
            System.out.println("Sent file transfer request for " + tPacket.getFilename());

            final FileOutputStream output = new FileOutputStream(fullFilePath, true);
            final long[] lastByteTime = {System.currentTimeMillis()};

            transferTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        byte[] data = new byte[1024];
                        int bytesRead = socket.getInputStream().read(data);
                        while (bytesRead != -1) {
                            lastByteTime[0] = System.currentTimeMillis();
                            System.out.println(bytesRead);
                            output.write(bytesRead);
                            bytesRead = socket.getInputStream().read(data);
                        }

                        if (lastByteTime[0] > System.currentTimeMillis() - 5000) {
                            System.out.println("Finished receiving " + tPacket.getFilename());

                            output.close();
                            transferTimer.cancel();
                            transferTimer.purge();
                        }
                    } catch (IOException e) {}
                }
            }, 0, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
