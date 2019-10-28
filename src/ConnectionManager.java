import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionManager extends Thread {
    private ServerSocket connectionSocket;
    private Map<String, ConnectionThread> syncConnections;

    private Map<Integer, Query> syncMessages;

    private boolean running = true;

    ConnectionManager(int port) throws IOException {
        System.out.println("Connection Port " + port);
        connectionSocket = new ServerSocket(port);

        HashMap<String, ConnectionThread> connections = new HashMap<>();
        syncConnections = Collections.synchronizedMap(connections);

        HashMap<Integer, Query> messages = new HashMap<>();
        syncMessages = Collections.synchronizedMap(messages);
    }
    
    @Override
    public void run() {
        try {
            while (running) {
                ConnectionThread ct = new ConnectionThread(this.connectionSocket.accept(), syncMessages, syncConnections);
                System.out.println("Accepting connection from: " + ct.getAddress());
                addConnection(ct);
            }
        } catch (IOException ignored) {}
    }
    
    void createNeighborConnections() throws IOException {
        Scanner neighborsIn = new Scanner(new File("./config_neighbors.txt"));
        
        while (neighborsIn.hasNextLine()) {
            String neighbor = neighborsIn.nextLine();
            String hostName = neighbor.split(" ")[0];
            System.out.println("Hostname: "+ hostName);
            int port = Integer.parseInt(neighbor.split(" ")[1]);
            System.out.println("Connecting on port " + port);
            InetAddress ip = InetAddress.getByName(hostName);
            System.out.println("Connecting to ip " + ip.getHostAddress());

            ConnectionThread ct = new ConnectionThread(new Socket(ip, port), syncMessages, syncConnections);
            System.out.println("Attempting to connect to: " + ct.getAddress());
            addConnection(ct);
        }
    }

    private void addConnection(ConnectionThread ct) throws IOException {
        if (syncConnections.get(ct.getAddress()) != null) {
            ct.close();
        } else if (!ct.isConnected()) {
            System.out.println("Failed to connect to " + ct.getAddress());
            ct.close();
        } else {
            System.out.println("Successfully connected to " + ct.getAddress());
            ct.start();
            syncConnections.put(ct.getAddress(), ct);
        }
    }

    void closeNeighborConnections() throws IOException {
        for (ConnectionThread ct : syncConnections.values()) {
            ct.close();
        }
        syncConnections.clear();
    }

    void exit() throws IOException {
        closeNeighborConnections();
        connectionSocket.close();
        running = false;
    }
    
    void get(String filename) throws IOException {
        Query q = new Query(filename);
        syncMessages.put(q.getId(), q);

        for (ConnectionThread ct : syncConnections.values()) {
            System.out.println("Query sent to: " + ct.getAddress());
            ct.sendMessage(q);
        }
    }
}
