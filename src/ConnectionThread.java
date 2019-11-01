import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ConnectionThread extends Thread {
    private Socket socket;

    // Maps for shared resources between threads
    private Map<Integer, Query> queries;
    private final Map<String, ConnectionThread> connections;

    // Timers for socket control
    private Timer readingTimer;
    private Timer heartbeatTimer;

    // Heartbeat info
    private Heartbeat lastHeartbeat;
    private long lastHeartbeatTime;
    private static final int TIMEOUT = 45000;

    ConnectionThread(Socket socket, Map<Integer, Query> queries,
                     Map<String, ConnectionThread> connections) {
        this.socket = socket;
        this.queries = queries;
        this.connections = connections;

        readingTimer = new Timer();
        heartbeatTimer = new Timer();
    }

    // Starts heartbeat and reads socket for inbound packets
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
                } catch (IOException ignored) {}
            }
        }, 0, 10);
    }

    boolean isConnected() {
        return socket.isConnected();
    }

    // Cancels timers and closes socket
    void close() throws IOException {
        readingTimer.cancel();
        readingTimer.purge();

        heartbeatTimer.cancel();
        heartbeatTimer.purge();

        socket.close();
    }

    // Parse the inbound messages
    private void parseBytes(byte[] data) throws IOException {
        StringBuilder message = new StringBuilder();
        for (byte chunk : data) {
            if ((char) chunk != '\n') {
                // If the message isn't over, keep appending
                message.append((char) chunk);
            } else {
                // Once the message is over, split it on the colon to find the message type
                String[] split = message.toString().split(":", 2);

                // Catches corrupt packets
                if (split.length != 2) {
                    return;
                }

                String type = split[0];
                String[] dataFields = split[1].split(";");
                switch (type) {
                    case "H":
                        // Heartbeat input
                        Heartbeat h = new Heartbeat(dataFields);
                        System.out.println("Received Heartbeat " + h.getId() + " from " + getAddress());
                        lastHeartbeatTime = System.currentTimeMillis();
                        break;
                    case "Q":
                        // Query input
                        Query q = new Query(dataFields);
                        System.out.println("Received Query for " + q.getFilename() + " from " + getAddress());
                        checkQuery(q);
                        break;
                    case "R":
                        //R Response input
                        Response r = new Response(dataFields);
                        System.out.println("Received Response for " + r.getFilename() + " from " + getAddress());
                        checkResponse(r);
                        break;
                }
                // Creates new message builder
                message = new StringBuilder();
            }
        }
    }

    // Process Query
    private void checkQuery(Query q) throws IOException {
        if (Books.getLocalBooks().contains(q.getFilename())) {
            // If the file is found, send response
            System.out.println("File found! Sending response for " + q.getFilename());
            sendMessage(new Response(q.getId(), q.getFilename()));
        } else if (queries.get(q.getId()) == null) {
            // If the file is not found, send query onwards and log that the query was at this host
            System.out.println("Host does not have requested file");
            q.setAddress(getAddress());
            queries.put(q.getId(), q);

            // Iterate over connections and send to everyone but this thread
            Collection<ConnectionThread> values = connections.values();
            
            synchronized (connections) {
                Iterator<ConnectionThread> it = values.iterator();

                while (it.hasNext()) {
                    ConnectionThread connection = it.next();
                    if (!connection.getAddress().equals(getAddress())) {
                        System.out.println("Query sent to: " + connection.getAddress());
                        connection.sendMessage(q);
                    }
                }
            }
        }
    }

    // Process Responses
    private void checkResponse(Response r) throws IOException {
        if (queries.containsKey(r.getId())) {
            // If the response corresponds to a query here, this is the backwards path
            String address = queries.get(r.getId()).getAddress();
            queries.remove(r.getId());
            if (address != null) {
                // The response is sent backwards to the original route
                connections.get(address).sendMessage(r);
            } else {
                // The response is back at sender, setup transfer
                System.out.println("Transfer Response for " + r.getFilename() + " at " + r.getIp() + ":" + r.getPort());
                TransferClient tc = new TransferClient(r);
                tc.start();
            }
        }
    }

    void sendMessage(ConnectionMessage message) throws IOException {
        byte[] mBytes = message.toString().getBytes();
        socket.getOutputStream().write(mBytes);
    }

    private void startHeartBeat() {
        // Loops over a timeout and last heartbeat message
        lastHeartbeatTime = System.currentTimeMillis();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (lastHeartbeatTime > (System.currentTimeMillis()) - TIMEOUT && socket.isConnected()) {
                        // Sends new heartbeat
                        lastHeartbeat = new Heartbeat();
                        System.out.println("Sent Heartbeat " + lastHeartbeat.getId() + " to " + getAddress());
                        sendMessage(lastHeartbeat);
                    } else {
                        // Timeout and close connection
                        System.out.println("No heartbeat. Closing connection to " + getAddress());
                        close();
                    }
                } catch (IOException ignored) {}
            }
        }, 0, 20000);
    }

    String getAddress() {
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
