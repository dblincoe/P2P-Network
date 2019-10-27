import java.util.Objects;
import java.util.Random;

public class Query extends ConnectionMessage {

    private int id;
    private String filename;
    private String address;

    public Query(String filename) {
        id = Math.abs(new Random().nextInt());
        this.filename = filename;
    }

    public Query(String[] data) {
        this.id = Integer.parseInt(data[1]);
        this.filename = data[2];
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public int getId() {
        return id;
    }

    public String getFilename() {
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
        return "Q;" + id + ";" + filename + "\n";
    }

}
