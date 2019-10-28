public class Transfer {

    private String filename;

    public Transfer(String filename) {
        this.filename = filename;
    }

    public Transfer(String[] message) {
        filename = message[1];
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "T;" + filename + "\n";
    }
}
