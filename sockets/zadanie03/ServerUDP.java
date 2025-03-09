import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

                ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength()).order(ByteOrder.LITTLE_ENDIAN);
                int littleEndianNumber = byteBuffer.getInt();

                byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
                byteBuffer.putInt(littleEndianNumber);

                int value = byteBuffer.getInt(0);
                System.out.println("[Server] Received request from client: " + value);
                value = value + 1;

                byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt(value);

                packet = new DatagramPacket(byteBuffer.array(), byteBuffer.array().length, packet.getAddress(), packet.getPort());
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
