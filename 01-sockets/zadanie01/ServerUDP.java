import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServerUDP {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        //
        String responseMsg = "I hear you, my client!";
        DatagramSocket serverSocket = null;
        int portNumber = 8080;

        try {
            serverSocket = new DatagramSocket(portNumber, InetAddress.getByName("127.0.0.1"));
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                serverSocket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println("[Server] Received request from: " + packet.getAddress() + ". Message: + request");

                byte[] response = responseMsg.getBytes(StandardCharsets.UTF_8);
                packet = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                serverSocket.send(packet);
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
