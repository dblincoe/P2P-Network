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
        System.out.println("Connected to " + socket.getInetAddress().toString() + ":" + socket.getPort());

        startHeartBeat();
        readingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[1024];
                    int bytesRead = socket.getInputStream().read(data);
                    while (bytesRead != -1) {
                        parseBytes(data);
                        bytesRead = socket.getInputStream().read(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 10);
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void close() throws IOException {
        connections.remove(getAddress());
        readingTimer.cancel();
        heartbeatTimer.cancel();
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
                    System.out.println("Received Query " + q.getId() + " from " + getAddress());
                    checkQuery(q);
                } else if (splitMessage[0].equals("R")) {
                    Response r = new Response(splitMessage);
                    System.out.println("Received Response " + r.getId() + " from " + getAddress());
                    checkResponse(r);
                }
                message = new StringBuilder();
            }
        }
    }

    private void checkQuery(Query q) throws IOException {
        if (Books.getLocalBooks().contains(q.getFilename())) {
            sendMessage(new Response(q.getId(), q.getFilename()));
        } else {
            q.setAddress(getAddress());
            queries.put(q.getId(), q);
            for (ConnectionThread connection : connections.values()) {
                System.out.println(connection);
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
                TransferClient tc = new TransferClient(r);
                tc.start();
            }
        }
    }

    private void sendMessage(ConnectionMessage message) throws IOException {
        byte[] mBytes = message.toString().getBytes();
        socket.getOutputStream().write(mBytes);
    }

    private void startHeartBeat() {
        lastHeartbeatTime = System.currentTimeMillis();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (lastHeartbeatTime > (System.currentTimeMillis()) - TIMEOUT) {
                        lastHeartbeat = new Heartbeat();
                        sendMessage(lastHeartbeat);
                        System.out.println("Sent Heartbeat " + lastHeartbeat.getId() + " to " + getAddress());
                    } else {
                        close();
                        System.out.println("Heartbeat not received");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
