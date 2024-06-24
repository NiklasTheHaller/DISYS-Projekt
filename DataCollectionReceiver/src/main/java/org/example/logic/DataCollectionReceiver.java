package org.example.logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.example.config.RabbitMQConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class DataCollectionReceiver {

    private static final String INPUT_QUEUE = "dataCollectionReceiverOutputQueue";
    private static final String OUTPUT_QUEUE = "pdfGeneratorQueue";
    static final Map<String, JSONArray> jobData = new HashMap<>();
    static final Map<String, Integer> jobCounts = new HashMap<>();
    static final Map<String, Integer> jobReceived = new HashMap<>();
    static final Map<String, String> customerNames = new HashMap<>();
    private Channel channel;

    public DataCollectionReceiver(Channel channel) {
        this.channel = channel;
    }

    public void processMessage(String message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(message);

        if (jsonNode.has("jobStart")) {
            String customerId = jsonNode.get("customerId").asText();
            String customerName = jsonNode.get("customerName").asText();
            int totalMessages = jsonNode.get("totalMessages").asInt();
            jobData.put(customerId, new JSONArray());
            jobCounts.put(customerId, totalMessages);
            jobReceived.put(customerId, 0);
            customerNames.put(customerId, customerName);
        } else {
            String customerId = jsonNode.get("customerId").asText();
            JsonNode chargesNode = jsonNode.get("charges");

            JSONArray dataArray = jobData.get(customerId);
            Iterator<JsonNode> iterator = chargesNode.iterator();
            while (iterator.hasNext()) {
                dataArray.put(iterator.next());
            }

            int received = jobReceived.get(customerId) + 1;
            jobReceived.put(customerId, received);

            if (received == jobCounts.get(customerId)) {
                JSONObject aggregatedData = new JSONObject();
                aggregatedData.put("customerId", customerId);
                aggregatedData.put("customer", "Customer: " + customerNames.get(customerId));
                aggregatedData.put("charges", dataArray);
                channel.basicPublish("", OUTPUT_QUEUE, null, aggregatedData.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public static void main(String[] args) {
        RabbitMQConfig config = new RabbitMQConfig();
        try (Connection connection = config.createConnection();
             Channel channel = connection.createChannel()) {

            config.setupQueue(channel, INPUT_QUEUE);
            config.setupQueue(channel, OUTPUT_QUEUE);

            DataCollectionReceiver receiver = new DataCollectionReceiver(channel);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    System.out.println(" [x] Received Data: '" + message + "'");
                    receiver.processMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            channel.basicConsume(INPUT_QUEUE, true, deliverCallback, consumerTag -> {});

            // Keep the application running
            CountDownLatch latch = new CountDownLatch(1);
            latch.await();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
