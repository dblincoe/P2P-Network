import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Ping {

    private String ip;
    private int port;


    public Ping() throws IOException {
        Socket s = new Socket("www.google.com", 80);
        this.ip = s.getLocalAddress().getHostAddress();
        s.close();

        Scanner portIn = new Scanner(new File("./config_peer.txt"));
        portIn.nextInt();
        portIn.nextInt();
        this.port = portIn.nextInt();
    }

    public Ping(String[] data) {
        ip = data[1];
        port = Integer.parseInt(data[2]);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "PI:" + ip + ":" + port + "\n";
    }
}
