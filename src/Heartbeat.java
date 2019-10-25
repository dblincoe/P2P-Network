import java.util.Objects;
import java.util.Random;

public class Heartbeat extends ConnectionMessage {

    private int id;

    public Heartbeat() {
        id = Math.abs(new Random().nextInt());
    }

    public Heartbeat(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return "H;" + id;
    }
}
