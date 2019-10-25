import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionManager extends Thread {
    private ServerSocket connectionSocket;
    private HashMap<String, ConnectionThread> connections;

    private HashMap<Integer, ConnectionMessage> messages;

    private boolean running = true;

    public ConnectionManager(int port) throws IOException {
        connectionSocket = new ServerSocket(port);
        connections = new HashMap<>();
        messages = new HashMap<>();
    }
    
    @Override
    public void run()
    {
        System.out.println("Connection Server Running");
        try
        {
            while (running)
            {
                Socket s = this.connectionSocket.accept();
                addConnection(s);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void createNeighborConnections() throws IOException
    {
        Scanner neighborsIn = new Scanner(new File("./config_neighbors.txt"));
        
        while (neighborsIn.hasNextLine()) {
            String neighbor = neighborsIn.nextLine();
            String hostName = neighbor.split(" ")[0];
            int port = Integer.parseInt(neighbor.split(" ")[1]);
    
            InetAddress ip = InetAddress.getByName(hostName);
            Socket s = new Socket(ip, port);

            addConnection(s);
        }
    }

    private boolean addConnection(Socket s) throws IOException {
        ConnectionThread ct = new ConnectionThread(s, messages, connections);
        if (connections.get(ct.getAddress()) != null) {
            ct.close();
            return false;
        } else {
            System.out.println("Connecting to " + ct.getAddress());
            ct.start();
            connections.put(ct.getAddress(), ct);
            return true;
        }
    }

    public void closeNeighborConnections() throws IOException
    {
        for (ConnectionThread ct : connections.values()) {
            ct.close();
        }
        connections.clear();
    }

    public void exit() throws IOException
    {
        closeNeighborConnections();
        connectionSocket.close();
        running = false;
    }
    
    public void get(String filename) {
        Query q = new Query(filename, null);
    }
}
