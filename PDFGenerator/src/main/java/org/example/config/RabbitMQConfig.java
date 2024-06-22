package org.example.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConfig {

    private static final String HOST = "localhost";
    private static final int PORT = 30003;

    public Connection createConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        return factory.newConnection();
    }

    public void setupQueue(Channel channel, String queueName) throws IOException {
        channel.queueDeclare(queueName, false, false, false, null);
    }
}
