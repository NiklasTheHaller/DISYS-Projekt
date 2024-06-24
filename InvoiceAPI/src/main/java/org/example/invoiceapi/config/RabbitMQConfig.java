package org.example.invoiceapi.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final String HOST = "localhost";
    private static final int PORT = 30003;

    public static final String INPUT_QUEUE = "inputQueue";
    public static final String METADATA_QUEUE = "invoiceMetadataQueue";

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(HOST);
        connectionFactory.setPort(PORT);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public Queue inputQueue() {
        return new Queue(INPUT_QUEUE, false);
    }

    @Bean
    public Queue metadataQueue() {
        return new Queue(METADATA_QUEUE, false);
    }
}
