import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientUDP {
    public static void main(String[] args) throws UnknownHostException, SocketException {
        DatagramSocket clientSocket = null;
        int portNumber = 9090;

        int serverPortNumber = 8080;
        String serverAddress = "127.0.0.1";
        byte[] buffer = new byte[1024];

        try {
            clientSocket = new DatagramSocket(portNumber, InetAddress.getByName("127.0.0.2"));
            byte[] request = "Hello there, server!".getBytes();

            DatagramPacket packet = new DatagramPacket(request, request.length, InetAddress.getByName(serverAddress), serverPortNumber);
            clientSocket.send(packet);

            packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(serverAddress), serverPortNumber);
            clientSocket.receive(packet);
            String response = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            System.out.println("[Client] Received response from server: " + response);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }
}
