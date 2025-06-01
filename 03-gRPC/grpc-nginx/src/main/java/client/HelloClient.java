package client;

import app.pb.HelloRequest;
import app.pb.HelloResponse;
import app.pb.HelloServiceGrpc;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HelloClient {
    ManagedChannel channel;
    HelloServiceGrpc.HelloServiceBlockingStub blockingStub;
    HelloServiceGrpc.HelloServiceStub asyncStub;

    public HelloClient(String host, int port) {
        this(NettyChannelBuilder.forAddress(host, port).usePlaintext());
    }
    public HelloClient(String host, int port, SslContext sslContext) {
        this(NettyChannelBuilder.forAddress(host, port), sslContext);
    }

    private HelloClient(NettyChannelBuilder channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
        asyncStub = HelloServiceGrpc.newStub(channel);
    }
    private HelloClient(NettyChannelBuilder channelBuilder, SslContext sslContext) {
        channel = channelBuilder
                .sslContext(sslContext)
                .build();

        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
        asyncStub = HelloServiceGrpc.newStub(channel);
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

        HelloRequest req = HelloRequest.newBuilder().setName("Alice").build();

        List<Thread> threads = new ArrayList<Thread>();

        for (int a = 0; a < 100; a++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    CountDownLatch latch = new CountDownLatch(1);

                    StreamObserver<HelloResponse> stream = new StreamObserver<HelloResponse>() {
                        @Override
                        public void onNext(HelloResponse rsp) {
                            if (Caller.calls.containsKey(rsp.getPort())) {
                                Caller.calls.put(rsp.getPort(), Caller.calls.get(rsp.getPort()) + 1);
                            } else {
                                Caller.calls.put(rsp.getPort(), 1);
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                            latch.countDown();
                        }

                        @Override
                        public void onCompleted() {
                            latch.countDown();
                        }
                    };

                    client.asyncStub.hello(req, stream);
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println(Caller.calls);
        client.shutdown();
    }
}

class Caller {
    public static ConcurrentHashMap<Integer, Integer> calls = new ConcurrentHashMap<>();
}