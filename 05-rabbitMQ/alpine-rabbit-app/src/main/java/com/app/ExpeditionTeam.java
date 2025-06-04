package com.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ExpeditionTeam {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String teamName;

    private final Channel channel;
    private final Connection connection;

    public ExpeditionTeam(String teamName) throws IOException, TimeoutException {
        this.teamName = teamName;

        this.connection = setupConnection();
        this.channel = setupChannel();

        setupQueues();
        listenForConfirmation();
    }

    void setupQueues() throws IOException {
        String pattern = String.format("confirm.%s.*", teamName);
        String queueName = "confirm." + teamName;

        this.channel.queueDeclare(queueName, false, false, false, null);
        this.channel.queueBind(queueName, RabbitMQConfig.CONFIRM_EXCHANGE, pattern);
    }

    public void listenForConfirmation() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String jsonMessage = new String(delivery.getBody());
            OrderMessage orderMessage = mapper.readValue(jsonMessage, OrderMessage.class);

            String formatted = String.format("[Team: %s] Received confirmation for product [%s]. Order ID: [%s]",
                    orderMessage.getTeamName(), orderMessage.getProductName(), orderMessage.getOrderID());
            System.out.println(formatted);

        };

        String queueName = "confirm." + teamName;
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }

    public void sendOrder(String productName) throws IOException {
        OrderMessage orderMessage = new OrderMessage(this.teamName, "", productName);

        ObjectMapper mapper = new ObjectMapper();
        String jsonMessage = mapper.writeValueAsString(orderMessage);

        channel.basicPublish(RabbitMQConfig.ORDER_EXCHANGE, "order." + productName, null, jsonMessage.getBytes());

        String formatted = String.format("[Team: %s] Send order for product: %s", this.teamName, productName);
        System.out.println(formatted);
    }

    private Connection setupConnection() throws IOException, TimeoutException {
        return RabbitMQConfig.getConnectionFactory().newConnection();
    }

    private Channel setupChannel() throws IOException {
        closeConnectionHook();
        return connection.createChannel();
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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("|> Provide team name: ");
        String teamName = scanner.nextLine().trim();

        while (true) {
            try {
                System.out.println("|> Provide product name to order: ");
                String productName = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(productName)) break;

                ExpeditionTeam expeditionTeam = new ExpeditionTeam(teamName);
                expeditionTeam.sendOrder(productName);
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Exiting...");
    }
}
