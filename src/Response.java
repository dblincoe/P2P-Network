import java.util.Objects;
import java.util.Random;

public class Response extends ConnectionMessage {

    private int id;
    private String ip;
    private int port;
    private String filename;

    public Response(int id, String ip, int port, String filename) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "R;" + id + ";" + ip + ":" + port + ";" + filename;
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
