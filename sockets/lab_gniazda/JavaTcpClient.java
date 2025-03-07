import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class JavaTcpClient {

    public static void main(String[] args) throws IOException {

        System.out.println("JAVA TCP CLIENT");
        String hostName = "localhost";
        int portNumber = 12345;
        Socket socket = null;

        try {
            // create socket
            socket = new Socket(hostName, portNumber);

            // in & out streams
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send msg, read response
            out.println("Ping Java Tcp");
            String response = in.readLine();
            System.out.println("received response: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null){
                socket.close();
            }
        }
    }

}
