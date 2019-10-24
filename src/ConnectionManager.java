import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionManager extends Thread {
    private ServerSocket connectionSocket;
    private List<ConnectionThread> connections;
    private List<ConnectionThread> neighbors;
    
    private boolean serverRunning;
    
    public ConnectionManager(int port) throws IOException {
        connectionSocket = new ServerSocket(port);
        connections = new ArrayList<>();
        neighbors = new ArrayList<>();
    }
    
    @Override
    public void run()
    {
        serverRunning = true;
        System.out.println("Connection Server Running");
        
        try
        {
            while (serverRunning)
            {
                Socket s = this.connectionSocket.accept();
                System.out.println("New Connection Accepted");
    
                ConnectionThread ct = new ConnectionThread(s);
                ct.start();
                connections.add(ct);
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
            Integer port = Integer.parseInt(neighbor.split(" ")[1]);
    
            InetAddress ip = InetAddress.getByName(hostName);
            System.out.println("Attempting to connecting to " + ip.toString() + ":" + port);
            
            Socket s = new Socket(ip, port);
            
            
            ConnectionThread ct = new ConnectionThread(s);
            ct.start();
            neighbors.add(ct);
        }
    }

    public void closeNeighborConnections() throws IOException
    {
        for (ConnectionThread ct : neighbors) {
            ct.close();
        }
        neighbors.clear();
    }

    public void exit() throws IOException
    {
        closeNeighborConnections();
        connectionSocket.close();
    }
    
    public void get(String filename) {
        Query q = new Query(filename);
    }
}
