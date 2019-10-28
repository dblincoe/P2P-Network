import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ConnectionThread extends Thread {
    private Socket socket;

    private HashMap<Integer, Query> queries;
    private HashMap<String, ConnectionThread> connections;

    private Timer readingTimer;
    private Timer heartbeatTimer;

    private Heartbeat lastHeartbeat;
    private long lastHeartbeatTime;
    private static final int TIMEOUT = 45000;

    public ConnectionThread(Socket socket, HashMap<Integer, Query> queries,
                            HashMap<String, ConnectionThread> connections) {
        this.socket = socket;

        this.queries = queries;
        this.connections = connections;

        readingTimer = new Timer();
        heartbeatTimer = new Timer();
    }

    @Override
    public void run() {
        startHeartBeat();
        readingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[1024];
                    int bytesRead = socket.getInputStream().read(data);
                    while (bytesRead != -1 && socket.isConnected()) {
                        parseBytes(data);
                        bytesRead = socket.getInputStream().read(data);
                    }
                } catch (IOException e) {}
            }
        }, 0, 10);
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void close() throws IOException {
        connections.remove(getAddress());

        readingTimer.cancel();
        readingTimer.purge();

        heartbeatTimer.cancel();
        heartbeatTimer.purge();

        socket.close();
    }

    private void parseBytes(byte[] data) throws IOException {
        StringBuilder message = new StringBuilder();
        for (byte chunk : data) {
            if ((char) chunk != '\n') {
                message.append((char) chunk);
            } else {
                String[] splitMessage = message.toString().split(";");
                if (splitMessage[0].equals("H")) {
                    Heartbeat h = new Heartbeat(splitMessage);
                    System.out.println("Received Hearbeat " + h.getId() + " from " + getAddress());
                    lastHeartbeatTime = System.currentTimeMillis();
                } else if (splitMessage[0].equals("Q")) {
                    Query q = new Query(splitMessage);
                    System.out.println("Received Query for " + q.getFilename() + " from " + getAddress());
                    checkQuery(q);
                } else if (splitMessage[0].equals("R")) {
                    Response r = new Response(splitMessage);
                    System.out.println("Received Response for " + r.getFilename() + " from " + getAddress());
                    checkResponse(r);
                }
                message = new StringBuilder();
            }
        }
    }

    private void checkQuery(Query q) throws IOException {
        if (Books.getLocalBooks().contains(q.getFilename())) {
            System.out.println("File found! Sending response for " + q.getFilename());
            sendMessage(new Response(q.getId(), q.getFilename()));
        } else {
            System.out.println("Host does not have requested file");
            q.setAddress(getAddress());
            queries.put(q.getId(), q);
            for (ConnectionThread connection : connections.values()) {
                if (!connection.getAddress().equals(getAddress())) {
                    System.out.println("Query sent to: " + connection.getAddress());
                    System.out.println(connection);
                }
            }
        }
    }

    private void checkResponse(Response r) throws IOException {
        if (queries.containsKey(r.getId())) {
            String address = queries.get(r.getId()).getAddress();
            queries.remove(r.getId());
            if (address != null) {
                connections.get(address).sendMessage(r);
            } else {
                System.out.println("Response recieved for " + r.getFilename() + " at " + r.getIp() + ":" + r.getPort());
                TransferClient tc = new TransferClient(r);
                tc.start();
            }
        }
    }

    public void sendMessage(ConnectionMessage message) throws IOException {
        byte[] mBytes = message.toString().getBytes();
        socket.getOutputStream().write(mBytes);
    }

    private void startHeartBeat() {
        lastHeartbeatTime = System.currentTimeMillis();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (lastHeartbeatTime > (System.currentTimeMillis()) - TIMEOUT && socket.isConnected()) {
                        lastHeartbeat = new Heartbeat();
                        System.out.println("Sent Heartbeat " + lastHeartbeat.getId() + " to " + getAddress());
                        sendMessage(lastHeartbeat);
                    } else {
                        System.out.println("No heartbeat. Closing connection to " + getAddress());
                        close();
                    }
                } catch (IOException e) {}
            }
        }, 0, 15000);
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
