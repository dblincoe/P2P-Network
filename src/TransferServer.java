import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class TransferServer extends Thread {

    Socket socket;
    Timer transferTimer;

    public TransferServer(Socket s) throws IOException {
        socket = s;
        transferTimer = new Timer();
    }

    @Override
    public void run() {
        transferTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[1024];
                    int bytesRead = socket.getInputStream().read(data);
                    while (bytesRead != -1) {
                        parseBytes(data);
                        bytesRead = socket.getInputStream().read(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 10);
    }

    private void parseBytes(byte[] data) throws IOException {
        StringBuilder message = new StringBuilder();
        for (byte chunk : data) {
            if ((char) chunk != '\n') {
                message.append((char) chunk);
            } else {
                String[] splitMessage = message.toString().split(";");
                if (splitMessage[0].equals("T")) {
                    Transfer t = new Transfer(splitMessage);
                    sendFile(readInFile(t.getFilename()));
                }
                transferTimer.cancel();
                break;
            }
        }
    }

    private void sendFile(String fileData) throws IOException {
        byte[] mBytes = fileData.getBytes();
        socket.getOutputStream().write(mBytes);
    }

    private String readInFile(String filename) throws FileNotFoundException {
        String filePath = "./shared/" + filename;
        Scanner fileIn = new Scanner(new File(filePath));
        String fileData = "";

        while (fileIn.hasNext()) {
            fileData += fileIn.next();
        }
        return fileData;
    }

    public String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public void close() throws IOException {
        transferTimer.cancel();
        socket.close();
    }
}

