package server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class HelloServer {
    private static final Logger logger = Logger.getLogger(HelloServer.class.getName());

    private final Server server;
    int port;

    public HelloServer(int port) {
        this(NettyServerBuilder.forPort(port), port);
    }

    public HelloServer(int port, SslContext sslContext) {
        this(NettyServerBuilder.forPort(port).sslContext(sslContext), port);
    }

    public HelloServer(NettyServerBuilder serverBuilder, int port) {
        this.port = port;

        HelloService service = new HelloService();
        this.server = serverBuilder.addService(service).build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("shut down gRPC server because JVM shuts down");
                try {
                    HelloServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("server shut down");
            }
        });
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static SslContext loadTLSCredentials() throws SSLException {
        File serverCertFile = new File("cert/server-cert.pem");
        File serverKeyFile = new File("cert/server-key.pem");

        File clientCACertFile = new File("cert/ca-cert.pem");

        SslContextBuilder builder = SslContextBuilder
                .forServer(serverCertFile, serverKeyFile)
                .clientAuth(ClientAuth.REQUIRE)
                .trustManager(clientCACertFile);

        return GrpcSslContexts.configure(builder).build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SslContext sslContext = loadTLSCredentials();

        HelloServer server = new HelloServer(8080, sslContext);
        server.start();
        server.blockUntilShutdown();
    }
}
