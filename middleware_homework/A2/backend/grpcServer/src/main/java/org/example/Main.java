package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.generator.CurrencyGenerator;
import org.example.server.CurrencyServiceImpl;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        CurrencyServiceImpl currencyService = new CurrencyServiceImpl();
        Server server = ServerBuilder.forPort(9090)
                .addService(currencyService)
                .build();

        server.start();


        while (true) {
            CurrencyServiceImpl.notifySubscribers(CurrencyGenerator.generateValue());
            Thread.sleep(1000);
        }

    }
}