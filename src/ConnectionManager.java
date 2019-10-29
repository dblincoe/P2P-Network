import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionManager extends Thread {
    private ServerSocket connectionSocket;
    private final Map<String, ConnectionThread> syncConnections;

    private Discovery discoverySocket;

    private Map<Integer, Query> syncMessages;

    private boolean running = true;

    ConnectionManager(int port, int discoveryPort) throws IOException {
        System.out.println("Connection Port " + port);
        connectionSocket = new ServerSocket(port);

        HashMap<String, ConnectionThread> connections = new HashMap<>();
        syncConnections = Collections.synchronizedMap(connections);

        discoverySocket = new Discovery(discoveryPort);
        discoverySocket.start();

        HashMap<Integer, Query> messages = new HashMap<>();
        syncMessages = Collections.synchronizedMap(messages);
    }
    
    @Override
    public void run() {
        try {
            while (running) {
                ConnectionThread ct = new ConnectionThread(this.connectionSocket.accept(), syncMessages, syncConnections);
                addConnection(ct, "Accepting connection from: ");
            }
        } catch (IOException ignored) {}
    }

    void runDiscoveryProtocol(String ip, int port) throws IOException {
        discoverySocket.sendInitPing(this, ip, port);
    }

    void createNeighbor(String hostIp, int port) throws IOException {
        InetAddress ip = InetAddress.getByName(hostIp);

        ConnectionThread ct = new ConnectionThread(new Socket(ip, port), syncMessages, syncConnections);
        addConnection(ct, "Attempting to connect to: ");
    }

    private void addConnection(ConnectionThread ct, String connStr) throws IOException {
        ConnectionThread prevConn = syncConnections.get(ct.getAddress());

        if (prevConn == null || !prevConn.isConnected()) {
            System.out.println(connStr + ct.getAddress());
        } else {
            prevConn.close();
        }

        if (!ct.isConnected()) {
            System.out.println("Failed to connect to " + ct.getAddress());
            ct.close();
        } else {
            if (prevConn == null || !prevConn.isConnected()) {
                System.out.println("Successfully connected to " + ct.getAddress());
            }

            ct.start();
            syncConnections.put(ct.getAddress(), ct);
        }
    }

    void closeNeighborConnections() throws IOException {
        Collection<ConnectionThread> values = syncConnections.values();

        synchronized (syncConnections) {
            Iterator<ConnectionThread> it = values.iterator();

            while (it.hasNext()) {
                ConnectionThread connection = it.next();
                connection.close();
            }
        }
        syncConnections.clear();
    }

    void exit() throws IOException {
        closeNeighborConnections();
        discoverySocket.close();
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
