import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class Response extends ConnectionMessage {

    private int id;
    private String ip;
    private int port;
    private String filename;

    Response(int id, String filename) throws IOException {
        this.id = id;

        Socket s = new Socket("www.google.com", 80);
        this.ip = s.getLocalAddress().getHostAddress();
        s.close();

        Scanner portIn = new Scanner(new File("./config_peer.txt"));
        portIn.nextInt();
        this.port = portIn.nextInt();

        this.filename = filename;
    }

    Response(String[] data) {
        this.id = Integer.parseInt(data[1]);
        this.ip = data[2].split(":")[0];
        this.port = Integer.parseInt(data[2].split(":")[1]);
        this.filename = data[3];
    }

    int getId() {
        return id;
    }

    String getIp() {
        return ip;
    }

    int getPort() {
        return port;
    }

    String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "R;" + id + ";" + ip + ":" + port + ";" + filename + "\n";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response r = (Response) o;
        return id == r.id && port == r.port && ip.equals(r.ip) && filename.equals(r.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ip, port, filename);
    }
}
