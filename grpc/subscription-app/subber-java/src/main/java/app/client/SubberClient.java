package app.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import subber.pb.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SubberClient {

    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) throws IOException, InterruptedException {
        String clientUUID = UUID.randomUUID().toString();
        System.out.println("Starting client with UUID: " + clientUUID);

        ManagedChannel channel = ManagedChannelBuilder.forAddress("0.0.0.0", 8080)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();

        SubberGrpc.SubberBlockingStub blockingStub = SubberGrpc.newBlockingStub(channel);
        SubberGrpc.SubberStub asyncStub = SubberGrpc.newStub(channel);

        BlockingQueue<SubscriptionRequest> requestQueue = new LinkedBlockingQueue<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown signal.");
            running.set(false);
            channel.shutdownNow();
        }));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            ListOptionsResponse options = blockingStub.listOptions(ListOptionsRequest.newBuilder().build());
            System.out.println("[OPTIONS]:");
            System.out.println(options);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (running.get()) {
                    System.out.println("[NEW SUBSCRIPTION] Enter Asset Name (e.g. BTC):");
                    String assetName = reader.readLine();
                    System.out.println("[NEW SUBSCRIPTION] Enter Threshold (e.g. 30000):");
                    double threshold = Double.parseDouble(reader.readLine());
                    System.out.println("[NEW SUBSCRIPTION] Condition (greater/less):");
                    String condition = reader.readLine();

                    SubscriptionRequest.Builder builder = SubscriptionRequest.newBuilder()
                            .setClientUUID(clientUUID)
                            .setAssetName(AssetName.valueOf(assetName))
                            .setThreshold(threshold);

                    if ("greater".equalsIgnoreCase(condition)) {
                        builder.setGreaterThan(true);
                    } else if ("less".equalsIgnoreCase(condition)) {
                        builder.setLessThan(true);
                    } else {
                        System.out.println("Unknown condition! Try again...");
                        continue;
                    }

                    SubscriptionRequest request = builder.build();
                    requestQueue.put(request);
                    System.out.println("Added subscription request to queue.");
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Input error: " + e.getMessage());
            }
        });

        while (running.get()) {
            SubscriptionRequest request = requestQueue.take();
            System.out.println("Sending subscription request: " + request);

            try {
                var responseIterator = blockingStub.subscription(request);

                executor.submit(() -> {
                    try {
                        while (running.get() && responseIterator.hasNext()) {
                            SubscriptionResponse rsp = responseIterator.next();
                            PrettyResponsePrint(rsp);
                        }
                    } catch (StatusRuntimeException e) {
                        if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                            System.err.println("Server unavailable, retrying...");
                        } else if (e.getStatus().getCode() == Status.Code.CANCELLED) {
                            System.out.println("Stream cancelled, exiting...");
                            running.set(false);
                        } else {
                            System.err.println("Stream error: " + e.getMessage());
                            running.set(false);
                        }
                    } catch (Exception e) {
                        System.err.println("Error receiving messages: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("Error while sending subscription request: " + e.getMessage());
            }
        }

        System.out.println("Shutting down...");
        executor.shutdownNow();
        channel.shutdownNow();
    }

    private static void PrettyResponsePrint(SubscriptionResponse rsp) {
        Instant instant = Instant.ofEpochSecond(rsp.getCurrentTime().getSeconds(), rsp.getCurrentTime().getNanos());
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate = localDateTime.format(formatter);

        System.out.println("-------------");
        System.out.println("\tAsset name: " + rsp.getAssetName());
        System.out.println("\tCurrent price: " + rsp.getCurrentPrice());
        System.out.println("\tMessage: " + rsp.getMessage());
        System.out.println("\tTime: " + formattedDate);
        System.out.println("-------------");
    }
}