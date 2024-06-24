package org.example.logic;

import com.rabbitmq.client.Channel;
import org.example.model.Charge;
import org.example.repository.ChargeRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StationDataCollectorTest {

    private Channel channel;
    private ChargeRepository chargeRepository;
    private StationDataCollector collector;

    @BeforeEach
    public void setUp() {
        channel = mock(Channel.class);
        chargeRepository = mock(ChargeRepository.class);
        collector = new StationDataCollector(channel, chargeRepository);
    }

    @Test
    public void testProcessMessage() throws Exception {
        // Arrange
        String message = new JSONObject()
                .put("stationUrl", "jdbc:postgresql://localhost:5432/stationdb")
                .put("customerId", 1)
                .toString();
        List<Charge> charges = Arrays.asList(
                new Charge(1, 10.0F, 1),
                new Charge(2, 20.0F, 1)
        );

        when(chargeRepository.findChargesByCustomerId(1)).thenReturn(charges);

        // Act
        collector.processMessage(message);

        // Assert
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(channel, times(1)).basicPublish(eq(""), eq("dataCollectionReceiverOutputQueue"), eq(null), captor.capture());

        String sentMessage = new String(captor.getValue(), StandardCharsets.UTF_8);
        JSONObject sentJson = new JSONObject(sentMessage);

        assertEquals(1, sentJson.getInt("customerId"));
        JSONArray chargesArray = sentJson.getJSONArray("charges");
        assertEquals(2, chargesArray.length());
        JSONObject charge1 = chargesArray.getJSONObject(0);
        assertEquals(1, charge1.getInt("id"));
        assertEquals(10.0, charge1.getDouble("kwh"));
        assertEquals(1, charge1.getInt("customerId"));
        JSONObject charge2 = chargesArray.getJSONObject(1);
        assertEquals(2, charge2.getInt("id"));
        assertEquals(20.0, charge2.getDouble("kwh"));
        assertEquals(1, charge2.getInt("customerId"));
    }
}
