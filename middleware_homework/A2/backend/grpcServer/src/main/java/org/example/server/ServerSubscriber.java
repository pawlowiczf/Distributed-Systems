package org.example.server;

import io.grpc.stub.StreamObserver;
import org.example.CurrencyEnum;
import org.example.CurrencyMessage;

import java.util.Objects;

public class ServerSubscriber {
    private final String userId;
    private final CurrencyEnum currencyEnum;
    private StreamObserver<CurrencyMessage> responseObserver;

    public ServerSubscriber(String userId, CurrencyEnum currencyEnum, StreamObserver<CurrencyMessage> responseObserver) {
        this.userId = userId;
        this.currencyEnum = currencyEnum;
        this.responseObserver = responseObserver;
    }

    public String getUserId() {
        return userId;
    }

    public CurrencyEnum getCurrencyEnum() {
        return currencyEnum;
    }

    public void setResponseObserver(StreamObserver<CurrencyMessage> responseObserver) {
        this.responseObserver = responseObserver;
    }

    public StreamObserver<CurrencyMessage> getResponseObserver() {
        return responseObserver;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerSubscriber that = (ServerSubscriber) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}

