package org.example.logic;

import com.rabbitmq.client.Channel;
import org.example.model.Customer;
import org.example.model.Station;
import org.example.repository.CustomerRepository;
import org.example.repository.StationRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class DataCollectionDispatcherTest {

    private static final String DATA_COLLECTION_RECEIVER_OUTPUT_QUEUE = "dataCollectionReceiverOutputQueue";
    private static final String STATION_DATA_COLLECTOR_OUTPUT_QUEUE = "stationDataCollectorOutputQueue";

    private StationRepository stationRepository;
    private CustomerRepository customerRepository;
    private Channel channel;
    private DataCollectionDispatcher dispatcher;

    @BeforeEach
    public void setUp() {
        stationRepository = mock(StationRepository.class);
        customerRepository = mock(CustomerRepository.class);
        channel = mock(Channel.class);
        dispatcher = new DataCollectionDispatcher(stationRepository, customerRepository, channel);
    }

    @Test
    public void testProcessMessage_CustomerFound() throws Exception {
        // Arrange
        long startTime = System.currentTimeMillis();
        String input = new JSONObject().put("customerId", 1).put("startTime", startTime).toString();

        Customer mockCustomer = new Customer(1, "John", "Doe");
        when(customerRepository.findCustomerById(1)).thenReturn(mockCustomer);

        Station station1 = new Station(1, "http://station1.com", 40.7128, -74.0060);
        Station station2 = new Station(2, "http://station2.com", 34.0522, -118.2437);
        List<Station> stations = Arrays.asList(station1, station2);
        when(stationRepository.findAll()).thenReturn(stations);

        ArgumentCaptor<String> captorQueueName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> captorMessage = ArgumentCaptor.forClass(byte[].class);

        // Act
        dispatcher.processMessage(input);

        // Assert
        verify(channel, times(3)).basicPublish(eq(""), captorQueueName.capture(), isNull(), captorMessage.capture());

        List<String> capturedQueueNames = captorQueueName.getAllValues();
        List<byte[]> capturedMessages = captorMessage.getAllValues();

        assertEquals(DATA_COLLECTION_RECEIVER_OUTPUT_QUEUE, capturedQueueNames.get(0));
        JSONObject jobStartMessage = new JSONObject(new String(capturedMessages.get(0), StandardCharsets.UTF_8));
        assertTrue(jobStartMessage.getBoolean("jobStart"));
        assertEquals(1, jobStartMessage.getInt("customerId"));
        assertEquals("John Doe", jobStartMessage.getString("customerName"));
        assertEquals(2, jobStartMessage.getInt("totalMessages"));
        assertEquals(startTime, jobStartMessage.getLong("startTime"));

        assertEquals(STATION_DATA_COLLECTOR_OUTPUT_QUEUE, capturedQueueNames.get(1));
        JSONObject stationMessage1 = new JSONObject(new String(capturedMessages.get(1), StandardCharsets.UTF_8));
        assertEquals("http://station1.com", stationMessage1.getString("stationUrl"));
        assertEquals(1, stationMessage1.getInt("customerId"));
        assertEquals(startTime, stationMessage1.getLong("startTime"));

        assertEquals(STATION_DATA_COLLECTOR_OUTPUT_QUEUE, capturedQueueNames.get(2));
        JSONObject stationMessage2 = new JSONObject(new String(capturedMessages.get(2), StandardCharsets.UTF_8));
        assertEquals("http://station2.com", stationMessage2.getString("stationUrl"));
        assertEquals(1, stationMessage2.getInt("customerId"));
        assertEquals(startTime, stationMessage2.getLong("startTime"));
    }




    @Test
    public void testProcessMessage_CustomerNotFound() throws Exception {
        // Arrange
        long startTime = System.currentTimeMillis();
        String input = new JSONObject().put("customerId", 1).put("startTime", startTime).toString();

        when(customerRepository.findCustomerById(1)).thenReturn(null);

        // Act
        dispatcher.processMessage(input);

        // Assert
        verify(channel, never()).basicPublish(anyString(), anyString(), any(), any());
    }
}
