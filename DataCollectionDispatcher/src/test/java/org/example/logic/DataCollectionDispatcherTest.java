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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataCollectionDispatcherTest {

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
        String input = new JSONObject().put("customerId", 1).toString();
        Customer customer = new Customer(1, "John", "Doe");
        List<Station> stations = Arrays.asList(new Station(1, "url1"), new Station(2, "url2"));

        when(customerRepository.findCustomerById(1)).thenReturn(customer);
        when(stationRepository.findAll()).thenReturn(stations);

        // Act
        dispatcher.processMessage(input);

        // Assert
        verify(channel, times(1)).basicPublish(eq(""), eq("dataCollectionReceiverOutputQueue"), eq(null), any(byte[].class));
        verify(channel, times(2)).basicPublish(eq(""), eq("stationDataCollectorOutputQueue"), eq(null), any(byte[].class));
    }

    @Test
    public void testProcessMessage_CustomerNotFound() throws Exception {
        // Arrange
        String input = new JSONObject().put("customerId", 1).toString();

        when(customerRepository.findCustomerById(1)).thenReturn(null);

        // Act
        dispatcher.processMessage(input);

        // Assert
        verify(channel, never()).basicPublish(anyString(), anyString(), any(), any());
    }
}
