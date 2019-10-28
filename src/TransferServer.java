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
                    while (bytesRead != -1 && socket.isConnected()) {
                        parseBytes(data);
                        bytesRead = socket.getInputStream().read(data);
                    }
                } catch (IOException e) {}
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
                    System.out.println("Received File Transfer request for " + t.getFilename() + " from " + getAddress());
                    sendFile(readInFile(t.getFilename()));
                }
                transferTimer.cancel();
                transferTimer.purge();
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
        StringBuilder fileData = new StringBuilder();

        System.out.println("Path " + filePath);
        System.out.println("New " + new File(filePath).canRead());
        while (fileIn.hasNextLine()) {
            fileData.append(fileIn.nextLine());
        }

        System.out.println(fileData.toString());

        return fileData.toString();
    }

    public String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public void close() throws IOException {
        transferTimer.cancel();
        transferTimer.purge();

        socket.close();
    }
}

