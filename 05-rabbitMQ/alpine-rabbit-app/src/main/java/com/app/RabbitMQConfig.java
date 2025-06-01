package com.app;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {
    public static final String ORDER_EXCHANGE = "orders.exchange";
    public static final String CONFIRM_EXCHANGE = "confirm.exchange";

    private static final ConnectionFactory FACTORY = createConnectionFactory();

    private static ConnectionFactory createConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        return factory;
    }
    public static ConnectionFactory getConnectionFactory() {
        return FACTORY;
    }
}