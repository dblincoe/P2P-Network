import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

public class Discovery extends Thread {

    private DatagramSocket socket;
    private boolean running;

    private HashMap<String, Integer> pingPorts;

    private List<Pong> pongs;

    public Discovery(int sourcePort) throws SocketException {
        socket = new DatagramSocket(sourcePort);

        pingPorts = new HashMap<>();
        pongs = new ArrayList<>();
    }

    // Continuously looks for inbound UDP packets
    @Override
    public void run() {
        running = true;

        while (running) {
            try {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                parsePacket(buf);
            } catch (IOException ignored) {}
        }
    }

    public void close() {
        socket.close();
        running = false;
    }

    // Sends ping to reciepient and waits for all the responses
    public void sendInitPing(final ConnectionManager cm, String ip, int port) throws IOException {
        pingPorts.put(ip, port);
        sendPing(new Ping(), ip, port);

        // Wait 500 ms and then check pongs
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Random rand = new Random();
                    if (pongs.size() == 0) {
                        System.out.println("No Pongs received");
                        return;
                    }

                    // Choose two random hosts to connect to
                    for (int i = 0; i < 2; i++) {
                        if (pongs.size() > 0) {
                            Pong randPong = pongs.get(rand.nextInt(pongs.size()));
                            pongs.remove(randPong);
                            cm.createNeighbor(randPong.getIp(), randPong.getPort());
                        }
                    }
                } catch (IOException ignored) {}
            }
        }, 500);
    }

    // Same as ConnectionThread except for discovery
    private void parsePacket(byte[] data) throws IOException {
        StringBuilder message = new StringBuilder();
        for (byte chunk : data) {
            if ((char) chunk != '\n') {
                message.append((char) chunk);
            } else {
                String[] split = message.toString().split(":");
                switch (split[0]) {
                    case "PI":
                        Ping pi = new Ping(split);

                        // Exclude already seen pings
                        if (pingPorts.get(pi.getIp()) == null) {
                            System.out.println("Retrieved Ping from " + pi.getIp() + ":" + pi.getPort());

                            pingPorts.put(pi.getIp(), pi.getPort());
                            sendPong(pi);
                            forwardPing(pi);
                        }
                        break;
                    case "PO":
                        // Accumulates pongs
                        Pong po = new Pong(split);
                        System.out.println("Retrieved Pong from " + po.getIp() + ":" + po.getPort());

                        pongs.add(po);
                        break;
                }
            }
        }
    }

    // Pass on the ping to all other ip addresses
    private void forwardPing(Ping pi) throws IOException {
        for (Entry<String, Integer> entry : pingPorts.entrySet()) {
            String nextConnIp = entry.getKey();

            if (!nextConnIp.equals(pi.getIp())) {
                sendPing(pi, nextConnIp, entry.getValue());
            }
        }
    }

    // Sends out a pong
    private void sendPong(Ping pi) throws IOException {
        Pong po = new Pong();
        System.out.println("Sending Pong to " + pi.getIp() + ":" + pi.getPort());

        byte[] buf = po.toString().getBytes();
        InetAddress addr = InetAddress.getByName(pi.getIp());
        DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, pi.getPort());
        socket.send(packet);
    }

    // Sends out a ping
    public void sendPing(Ping pi, String ip, int port) throws IOException {
        System.out.println("Sending Ping to " + ip + ":" + port);

        byte[] buf = pi.toString().getBytes();
        InetAddress addr = InetAddress.getByName(ip);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, port);
        socket.send(packet);
    }
}
