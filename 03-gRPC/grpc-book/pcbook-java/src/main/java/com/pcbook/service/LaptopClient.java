package com.pcbook.service;

import com.pcbook.pb.CreateLaptopRequest;
import com.pcbook.pb.CreateLaptopResponse;
import com.pcbook.pb.Laptop;
import com.pcbook.pb.LaptopServiceGrpc;
import com.pcbook.pb.LaptopServiceGrpc.LaptopServiceBlockingStub;
import com.pcbook.sample.Generator;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LaptopClient {
    private static final Logger logger = Logger.getLogger(LaptopClient.class.getName());

    private final ManagedChannel channel;
    private final LaptopServiceBlockingStub blockingStub;

    public LaptopClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        blockingStub = LaptopServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void createLaptop(Laptop laptop) {
        CreateLaptopRequest req = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();
        CreateLaptopResponse rsp = CreateLaptopResponse.getDefaultInstance();

        try {
            rsp = blockingStub.createLaptop(req);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "request failed: " + e.getMessage());
        }

        logger.info("laptop created with ID: " + rsp.getId());
    }

    public static void main(String[] args) throws InterruptedException {
        Generator generator = new Generator();

        LaptopClient client = new LaptopClient("0.0.0.0", 8080);

        try {
            client.createLaptop(generator.NewLaptop());
        } finally {
            client.shutdown();
        }
    }
}
