import java.util.Objects;
import java.util.Random;

public class Heartbeat extends ConnectionMessage {

    private int id;

    Heartbeat() {
        id = Math.abs(new Random().nextInt());
    }

    Heartbeat(String[] data) {
        this.id = Integer.parseInt(data[0]);
    }

    int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Heartbeat h = (Heartbeat) o;
        return id == h.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "H:" + id + "\n";
    }
}
