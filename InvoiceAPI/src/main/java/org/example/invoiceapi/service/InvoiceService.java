package org.example.invoiceapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.invoiceapi.config.RabbitMQConfig;
import org.example.invoiceapi.model.InvoiceMetadata;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.handler.annotation.Payload;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class InvoiceService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String METADATA_QUEUE = "invoiceMetadataQueue";
    private ConcurrentMap<Integer, InvoiceMetadata> metadataMap = new ConcurrentHashMap<>();

    public void sendInvoiceRequest(int customerId) {
        // Create JSON message
        JSONObject message = new JSONObject();
        message.put("customerId", customerId);

        // Send message to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.INPUT_QUEUE, message.toString());
    }

    public Optional<InvoiceMetadata> getInvoiceMetadata(int customerId) {
        return Optional.ofNullable(metadataMap.get(customerId));
    }

    @RabbitListener(queues = METADATA_QUEUE)
    public void receiveMessage(@Payload String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(message);
            int customerId = jsonNode.get("customerId").asInt();
            String downloadLink = jsonNode.get("filePath").asText();
            long creationTime = jsonNode.get("creationTime").asLong();
            InvoiceMetadata metadata = new InvoiceMetadata(downloadLink, creationTime);
            metadataMap.put(customerId, metadata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
