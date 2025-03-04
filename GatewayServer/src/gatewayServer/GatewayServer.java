package gatewayServer;

import connectionService.ConnectionService;
import parser.JsonParser;
import rps.RPS;

import java.io.IOException;
import java.net.InetAddress;

public class GatewayServer {
    private final RPS rps;
    private final ConnectionService connectionService;

    public GatewayServer(String dirToWatch) {
        rps = new RPS(new JsonParser(), dirToWatch);
        connectionService = new ConnectionService(rps, "10.10.0.95");
    }

    public void addTCP(int port, InetAddress ip) throws IOException {
        connectionService.addTCP(port, ip);
    }

    public void addUDP(int port, InetAddress ip) throws IOException {
        connectionService.addUDP(port, ip);
    }

    public void start() throws IOException {
        connectionService.start();
    }

    public void shutdown() throws IOException, InterruptedException {
        connectionService.stop();
        rps.shutdown();
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        GatewayServer gatewayServer = new GatewayServer("JARfiles");

        gatewayServer.addTCP(60000, InetAddress.getByName("10.10.0.95"));
        gatewayServer.addUDP(60001, InetAddress.getByName("10.10.0.95"));

        try {
            gatewayServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread.sleep(60000);
    }

}