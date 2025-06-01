package com.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Supplier {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String supplierName;
    private final List<String> availableProducts;

    private final Channel channel;
    private final Connection connection;

    public Supplier(String supplierName, List<String> availableProducts) throws IOException, TimeoutException {
        this.supplierName = supplierName;
        this.availableProducts = availableProducts;

        this.connection = setupConnection();
        this.channel = setupChannel();

        this.setupExchanges();
        this.setupOrderQueues();
    }

    private Connection setupConnection() throws IOException, TimeoutException {
        return RabbitMQConfig.getConnectionFactory().newConnection();
    }

    private Channel setupChannel() throws IOException {
        closeConnectionHook();
        return connection.createChannel();
    }

    private void setupExchanges() throws IOException {
        this.channel.exchangeDeclare(RabbitMQConfig.ORDER_EXCHANGE, BuiltinExchangeType.DIRECT);
        this.channel.exchangeDeclare(RabbitMQConfig.CONFIRM_EXCHANGE, BuiltinExchangeType.TOPIC);
    }

    private void setupOrderQueues() throws IOException {
        for (String productName : availableProducts) {
            String queueName = "order." + productName;

            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, RabbitMQConfig.ORDER_EXCHANGE, "order." + productName);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String jsonMessage = new String(delivery.getBody());
                OrderMessage orderMessage = mapper.readValue(jsonMessage, OrderMessage.class);

                String formatted = String.format("[Supplier: %s] Received order from: [%s] for product [%s]",
                        this.supplierName, orderMessage.getTeamName(), orderMessage.getProductName());
                System.out.println(formatted);

                UUID orderID = UUID.randomUUID();
                orderMessage.setOrderID(orderID.toString());
                jsonMessage = mapper.writeValueAsString(orderMessage);

                String pattern = String.format("confirm.%s.%s", orderMessage.getTeamName(), orderMessage.getOrderID());
                channel.basicPublish(RabbitMQConfig.CONFIRM_EXCHANGE, pattern, null, jsonMessage.getBytes());
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        }
    }

    void closeConnectionHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.channel.close();
                this.connection.close();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }

        }));
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("|> Provide supplier name: ");
        String supplierName = scanner.nextLine().trim();

        System.out.println("|> Provide available products, i.e: oxygen,mask,jacket,helmet : ");
        List<String> availableProducts = Arrays.asList(scanner.nextLine().split(","));

        try {
            Supplier supplier = new Supplier(supplierName, availableProducts);
            System.out.println("Supplier ready to process messages");

            System.in.read();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
