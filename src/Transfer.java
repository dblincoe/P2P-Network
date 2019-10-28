public class Transfer {

    private String filename;

    Transfer(String filename) {
        this.filename = filename;
    }

    Transfer(String[] message) {
        filename = message[1];
    }

    String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "T;" + filename + "\n";
    }
}
