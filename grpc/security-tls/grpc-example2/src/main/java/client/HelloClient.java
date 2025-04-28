package client;

import app.pb.HelloRequest;
import app.pb.HelloResponse;
import app.pb.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class HelloClient {
    ManagedChannel channel;
    HelloServiceGrpc.HelloServiceBlockingStub blockingStub;

    public HelloClient(String host, int port) {
        this(NettyChannelBuilder.forAddress(host, port).usePlaintext());
    }
    public HelloClient(String host, int port, SslContext sslContext) {
        this(NettyChannelBuilder.forAddress(host, port), sslContext);
    }

    private HelloClient(NettyChannelBuilder channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
    }
    private HelloClient(NettyChannelBuilder channelBuilder, SslContext sslContext) {
        channel = channelBuilder
                .sslContext(sslContext)
                .build();

        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static SslContext loadTLSCredentials() throws SSLException {
        File serverCACertFile = new File("cert/ca-cert.pem");

        File clientCertFile = new File("cert/client-cert.pem");
        File clientKeyFile = new File("cert/client-key.pem");

        return GrpcSslContexts
                .forClient()
                .keyManager(clientCertFile, clientKeyFile)
                .trustManager(serverCACertFile)
                .build();
    }

    public static void main(String[] args) throws InterruptedException, SSLException {
        SslContext sslContext = loadTLSCredentials();

        HelloClient client = new HelloClient("localhost", 8080, sslContext);
        HelloResponse rsp = client.blockingStub.hello(HelloRequest.newBuilder().setName("Filip").build());
        System.out.println(rsp.getMessage());

        client.shutdown();
    }
}
