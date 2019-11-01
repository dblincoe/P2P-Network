
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class TransferServer extends Thread {

    private Socket socket;
    private Timer transferTimer;

    TransferServer(Socket s){
        socket = s;
        transferTimer = new Timer();
    }

    // Loops through and gets any new messages
    @Override
    public void run() {
        transferTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[1024];
                    int bytesRead = socket.getInputStream().read(data);
                    while (bytesRead != -1 && socket.isConnected()) {
                        parseBytes(data);
                        bytesRead = socket.getInputStream().read(data);
                    }
                } catch (IOException ignored) {}
            }
        }, 0, 10);
    }

    // Parses inbound message
    private void parseBytes(byte[] data) throws IOException {
        StringBuilder message = new StringBuilder();

        for (byte chunk : data) {
            if ((char) chunk != '\n') {
                message.append((char) chunk);
            } else {
                String[] split = message.toString().split(":", 2);

                if (split.length != 2) {
                    return;
                }

                String type = split[0];
                String[] dataFields = split[1].split(";");
                if (type.equals("T")) {
                    // Setup transfer and send file
                    Transfer t = new Transfer(dataFields);
                    System.out.println("Received File Transfer request for " + t.getFilename() + " from " + getAddress());

                    sendFile(t.getFilename());

                    close();
                    return;
                }
            }
        }
    }

    // Read in a filename and send the file over the tcp connection
    private void sendFile(String filename) throws IOException {
        String filePath = "./shared/" + filename;
        FileReader fileIn = new FileReader(filePath);

        char[] toSend = new char[1024];
        while (fileIn.read(toSend) != -1) {
            byte[] mBytes = new String(toSend).getBytes(StandardCharsets.US_ASCII);
            socket.getOutputStream().write(mBytes);
        }

        System.out.println("Finished sending " + filename + " to " + getAddress());
    }

    String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    void close() throws IOException {
        transferTimer.cancel();
        transferTimer.purge();

        socket.close();
    }
}

