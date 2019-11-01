import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class Books {
    // Returns set of the local books to be  found. Mostly just a helper
    static HashSet<String> getLocalBooks() throws FileNotFoundException {
        Scanner booksIn = new Scanner(new File("./config_sharing.txt"));
        HashSet<String> localBooks = new HashSet<>();
        while (booksIn.hasNextLine()) {
            localBooks.add(booksIn.nextLine());
        }
        return localBooks;
    }
}
