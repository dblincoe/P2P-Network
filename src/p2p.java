import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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

        // Reads in port numbers
        Scanner portIn = new Scanner(new File("./config_peer.txt"));
        int connectionPort = portIn.nextInt();
        int transferPort = portIn.nextInt();
        int discoveryPort = portIn.nextInt();

        // Determines local ip
        Socket s = new Socket("www.google.com", 80);
        System.out.println("IP Address " + s.getLocalAddress().getHostAddress());
        s.close();

        // Setup and start connection and transfer manager
        ConnectionManager connection = new ConnectionManager(connectionPort, discoveryPort);
        TransferManager transfer = new TransferManager(transferPort);
        System.out.println("Discovery Port " + discoveryPort);
        System.out.println("Transfer, Connection, and Discovery Sockets Opened");
        connection.start();
        transfer.start();
        
        // Loop over scanner input to read user commands
        Scanner commandsIn = new Scanner(System.in);
        String command = commandsIn.nextLine().toLowerCase();
        System.out.println();

        while (!command.equals("exit")) {
            if (command.contains("connect")) {
                connection.runDiscoveryProtocol(command.split(" ")[1], Integer.parseInt(command.split(" ")[2]));
            } else if (command.contains("get")) {
                connection.get(command.split(" ")[1]);
            } else if (command.equals("leave")) {
                System.out.println("Leaving Network");
                connection.closeNeighborConnections();
            } else {
                System.out.println("Unknown Command");
            }

            command = commandsIn.nextLine().toLowerCase();
            System.out.println();
        }
        System.out.println("Stopping Peer");
        connection.exit();
        transfer.exit();
        System.exit(0);
    }
}
