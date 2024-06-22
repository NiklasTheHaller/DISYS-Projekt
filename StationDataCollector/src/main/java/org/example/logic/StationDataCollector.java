package org.example.logic;

import com.rabbitmq.client.*;
import org.example.config.RabbitMQConfig;
import org.example.model.Charge;
import org.example.repository.ChargeRepository;
import org.example.repository.Db;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class StationDataCollector {

    private static final String INPUT_QUEUE = "stationDataCollectorOutputQueue";
    private static final String OUTPUT_QUEUE = "dataCollectionReceiverOutputQueue";

    public static void main(String[] args) {
        RabbitMQConfig config = new RabbitMQConfig();
        try (Connection connection = config.createConnection();
             Channel channel = connection.createChannel()) {

            config.setupQueue(channel, INPUT_QUEUE);
            config.setupQueue(channel, OUTPUT_QUEUE);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    JSONObject inputJson = new JSONObject(message);
                    String stationUrl = inputJson.getString("stationUrl");
                    int customerId = inputJson.getInt("customerId");

                    System.out.println(" [x] Received Station Data Request: '" + message + "'");

                    // Set the database connection for the station
                    Db.setDbUrl(stationUrl);

                    // Use ChargeRepository to get the charges for the customer
                    ChargeRepository chargeRepository = new ChargeRepository();
                    List<Charge> charges = chargeRepository.findChargesByCustomerId(customerId);

                    // Create JSON message
                    JSONObject outputJson = new JSONObject();
                    outputJson.put("customerId", customerId);
                    JSONArray chargesArray = new JSONArray();
                    for (Charge charge : charges) {
                        JSONObject chargeJson = new JSONObject();
                        chargeJson.put("id", charge.getId());
                        chargeJson.put("kwh", charge.getKwh());
                        chargeJson.put("customerId", charge.getCustomerId());
                        chargesArray.put(chargeJson);
                    }
                    outputJson.put("charges", chargesArray);

                    // Send the aggregated JSON data to Data Collection Receiver
                    channel.basicPublish("", OUTPUT_QUEUE, null, outputJson.toString().getBytes(StandardCharsets.UTF_8));
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
