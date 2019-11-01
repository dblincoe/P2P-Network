import java.util.Objects;
import java.util.Random;

// Query message containing just the id, filename and sending address
public class Query extends ConnectionMessage {

    private int id;
    private String filename;
    private String address;

    Query(String filename) {
        id = Math.abs(new Random().nextInt());
        this.filename = filename;
    }

    Query(String[] data) {
        this.id = Integer.parseInt(data[0]);
        this.filename = data[1];
    }

    void setAddress(String address) {
        this.address = address;
    }

    String getAddress() {
        return address;
    }

    int getId() {
        return id;
    }

    String getFilename() {
        return filename;
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
        return "Q:" + id + ";" + filename + "\n";
    }

}
