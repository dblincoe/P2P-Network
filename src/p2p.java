import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class p2p
{
    public static void main(String[] args) throws IOException {
        // Clears the obtained folder to the initial state
        File[] obtained = new File("./obtained").listFiles();
        for (File f : obtained) {
            if (f.isFile()) {
                f.delete();
            }
        }

        String hostName = InetAddress.getLocalHost().getHostName();
        String ip = InetAddress.getByName(hostName).getAddress().toString();

        Scanner portIn = new Scanner(new File("./config_peer.txt"));
        int connectionPort = portIn.nextInt();
        int transferPort = portIn.nextInt();

        TransferManager transfer = new TransferManager(transferPort);
        ConnectionManager connection = new ConnectionManager(connectionPort);

        Scanner commandsIn = new Scanner(System.in);
        String command = commandsIn.nextLine().toLowerCase();

        while (!command.equals("exit")) {
            if (command.equals("connect")) {
                connection.createNeighborConnections();
            } else if (command.contains("get")) {
                connection.get(command.split(" ")[1]);
            } else if (command.equals("leave")) {
                connection.closeNeighborConnections();
            }

            command = commandsIn.nextLine().toLowerCase();
        }
        connection.closeNeighborConnections();

    }
}
