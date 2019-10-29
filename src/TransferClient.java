import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TransferClient extends Thread {

    private Socket socket;
    private Transfer tPacket;

    private String fullFilePath;

    TransferClient(Response r) throws IOException {
        socket = new Socket(r.getIp(), r.getPort());
        tPacket = new Transfer(r.getFilename());

        fullFilePath = "./obtained/" + r.getFilename();
    }

    @Override
    public void run() {
        try {
            byte[] mBytes = tPacket.toString().getBytes();
            socket.getOutputStream().write(mBytes);
            System.out.println("Sent file transfer request for " + tPacket.getFilename());

            final FileOutputStream output = new FileOutputStream(fullFilePath, true);

            try {
                byte[] data = new byte[65536];
                InputStream is = socket.getInputStream();
                int bytesRead = is.read(data);

                while (bytesRead > 0) {
                    output.write(new String(data).getBytes(StandardCharsets.US_ASCII));
                    bytesRead = is.read(data, 0, is.available());
                }

                System.out.println("Finished receiving " + tPacket.getFilename());
            } catch (IOException ignored) {}

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
