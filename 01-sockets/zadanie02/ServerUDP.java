import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ServerUDP {
    public static void main(String[] args) {
        DatagramSocket serverSocket = null;
        int serverPort = 8080;

        byte[] buffer = new byte[1024];

        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket = new DatagramSocket(serverPort, InetAddress.getByName("127.0.0.1"));

            System.out.println("[Server] Started listening:");
            while (true) {
                serverSocket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                System.out.println("[Server] Received request from client: " + request);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}
