package org.example.logic;

import com.rabbitmq.client.*;
import org.example.config.RabbitMQConfig;
import org.example.model.Station;
import org.example.model.Customer;
import org.example.repository.StationRepository;
import org.example.repository.CustomerRepository;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class DataCollectionDispatcher {

    private static final String INPUT_QUEUE = "inputQueue";
    private static final String STATION_DATA_COLLECTOR_OUTPUT_QUEUE = "stationDataCollectorOutputQueue";
    private static final String DATA_COLLECTION_RECEIVER_OUTPUT_QUEUE = "dataCollectionReceiverOutputQueue";

    private StationRepository stationRepository;
    private CustomerRepository customerRepository;
    private Channel channel;

    public DataCollectionDispatcher(StationRepository stationRepository, CustomerRepository customerRepository, Channel channel) {
        this.stationRepository = stationRepository;
        this.customerRepository = customerRepository;
        this.channel = channel;
    }

    public void processMessage(String input) {
        try {
            JSONObject inputJson = new JSONObject(input);
            int customerId = inputJson.getInt("customerId");

            System.out.println(" [x] Received Customer ID: '" + customerId + "'");

            // Get all charging stations
            List<Station> stations = stationRepository.findAll();
            int totalStations = stations.size();

            // Get customer details
            Customer customer = customerRepository.findCustomerById(customerId);
            if (customer == null) {
                System.err.println("Customer with ID " + customerId + " not found.");
                return; // Early exit if customer is not found
            }

            // Send a message to Data Collection Receiver about the job start
            JSONObject jobStartMessage = new JSONObject();
            jobStartMessage.put("jobStart", true);
            jobStartMessage.put("customerId", customer.getId());
            jobStartMessage.put("customerName", customer.getFirstName() + " " + customer.getLastName());
            jobStartMessage.put("totalMessages", totalStations);

            channel.basicPublish("", DATA_COLLECTION_RECEIVER_OUTPUT_QUEUE, null, jobStartMessage.toString().getBytes(StandardCharsets.UTF_8));

            // Send a message for each station to Station Data Collector
            for (Station station : stations) {
                JSONObject stationMessage = new JSONObject();
                stationMessage.put("stationUrl", station.getDbUrl());
                stationMessage.put("customerId", customerId);
                channel.basicPublish("", STATION_DATA_COLLECTOR_OUTPUT_QUEUE, null, stationMessage.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RabbitMQConfig config = new RabbitMQConfig();
        StationRepository stationRepository = new StationRepository();
        CustomerRepository customerRepository = new CustomerRepository();
        try (Connection connection = config.createConnection();
             Channel channel = connection.createChannel()) {

            config.setupQueue(channel, INPUT_QUEUE);
            config.setupQueue(channel, STATION_DATA_COLLECTOR_OUTPUT_QUEUE);
            config.setupQueue(channel, DATA_COLLECTION_RECEIVER_OUTPUT_QUEUE);

            DataCollectionDispatcher dispatcher = new DataCollectionDispatcher(stationRepository, customerRepository, channel);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String input = new String(delivery.getBody(), StandardCharsets.UTF_8);
                dispatcher.processMessage(input);
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
