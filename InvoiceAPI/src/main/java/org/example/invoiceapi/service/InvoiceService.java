package org.example.invoiceapi.service;

import org.example.invoiceapi.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

@Service
public class InvoiceService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendInvoiceRequest(int customerId) {
        // Create JSON message
        JSONObject message = new JSONObject();
        message.put("customerId", customerId);

        // Send message to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.INPUT_QUEUE, message.toString());
    }
}
