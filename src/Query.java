import java.util.Objects;
import java.util.Random;

public class Query extends ConnectionMessage {

    private int id;
    private String filename;
    private String address;

    public Query(String filename, String address) {
        id = Math.abs(new Random().nextInt());
        this.filename = filename;
        this.address = address;
    }

    public Query(int id, String filename) {
        this.id = id;
        this.filename = filename;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query q = (Query) o;
        return id == q.id && filename.equals(q.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filename);
    }

    @Override
    public String toString() {
        return "Q;" + id + ";" + filename;
    }

}