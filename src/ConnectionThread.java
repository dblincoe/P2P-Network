import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConnectionThread extends Thread
{
    private Socket socket;
    private HashMap<Integer, ConnectionMessage> messages;
    private HashMap<String, ConnectionThread> connections;

    private boolean running;

    private long lastHeartbeatTime;
    private static final int TIMEOUT = 30000;
    
    public ConnectionThread(Socket socket, HashMap<Integer, ConnectionMessage> messages,
                            HashMap<String, ConnectionThread>  connections) {
        this.socket = socket;
        this.messages = messages;
        this.connections = connections;

        this.running = false;
    }
    
    @Override
    public void run() {
        System.out.println("Connected to " + socket.getInetAddress().toString() + ":" + socket.getPort());
        running = true;

        startHeartBeat();
        try {
            Scanner scanner = new Scanner(socket.getInputStream(), StandardCharsets.UTF_8.name());
            while(running) {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (scanner.hasNext()) {
                            parseMessage(scanner.nextLine().split(";"));
                        }
                    }
                },0, 10);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseMessage(String[] message) {
        synchronized (messages) {
            if (message[0].equals("H")) {
                lastHeartbeatTime = System.currentTimeMillis() / 1000;
            } else if (message[0].equals("Q")) {
                // TODO: Check if I have this file
            } else if (message[0].equals("R")) {
                //TODO: Check if this needs to propagate back or get file
            }
        }
    }

    public void sendMessage(ConnectionMessage message) {
        //TODO: Send Message
    }
    
    public void close() throws IOException
    {
        running = false;
        socket.close();
    }

    private void startHeartBeat() {
        lastHeartbeatTime = System.currentTimeMillis();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (lastHeartbeatTime > (System.currentTimeMillis()) - TIMEOUT) {
                    sendMessage(new Heartbeat());
                    System.out.println("Sent Heartbeat");
                } else {
                    try {
                        close();
                        System.out.println("Heartbeat not received");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        },0, TIMEOUT);
    }

    public String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionThread ct = (ConnectionThread) o;
        return getAddress().equals(ct.getAddress());
    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }
}
