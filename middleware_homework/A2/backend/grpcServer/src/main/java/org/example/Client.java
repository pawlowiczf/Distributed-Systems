package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class Client {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        CurrencyServiceGrpc.CurrencyServiceStub stub = CurrencyServiceGrpc.newStub(channel);

        UserRequest request = UserRequest.newBuilder()
                .setUserId("User123")
                .setCurrencyEnum(CurrencyEnum.EUR)
                .build();
        new Thread(() -> {
            stub.connect(request, new StreamObserver<CurrencyMessage>() {
                @Override
                public void onNext(CurrencyMessage value) {
                    System.out.println("Odebrano: " + value);
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    System.out.println("Połączenie zakończone.");
                }
            });
        }).start();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}