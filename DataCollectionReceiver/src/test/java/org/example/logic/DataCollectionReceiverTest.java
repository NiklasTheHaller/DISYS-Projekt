package org.example.logic;

import com.rabbitmq.client.Channel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataCollectionReceiverTest {

    private Channel channel;
    private DataCollectionReceiver receiver;

    @BeforeEach
    public void setUp() {
        channel = mock(Channel.class);
        receiver = new DataCollectionReceiver(channel);
    }

    @Test
    public void testProcessMessage_JobStart() throws Exception {
        // Arrange
        String message = new JSONObject()
                .put("jobStart", true)
                .put("customerId", "1")
                .put("customerName", "John Doe")
                .put("totalMessages", 2)
                .toString();

        // Act
        receiver.processMessage(message);

        // Assert
        assertTrue(DataCollectionReceiver.jobData.containsKey("1"));
        assertTrue(DataCollectionReceiver.jobCounts.containsKey("1"));
        assertTrue(DataCollectionReceiver.jobReceived.containsKey("1"));
        assertTrue(DataCollectionReceiver.customerNames.containsKey("1"));

        assertEquals(new JSONArray().toString(), DataCollectionReceiver.jobData.get("1").toString());
        assertEquals(2, DataCollectionReceiver.jobCounts.get("1"));
        assertEquals(0, DataCollectionReceiver.jobReceived.get("1"));
        assertEquals("John Doe", DataCollectionReceiver.customerNames.get("1"));
    }

    @Test
    public void testProcessMessage_JobData() throws Exception {
        // Arrange
        // Initialize with job start
        String jobStartMessage = new JSONObject()
                .put("jobStart", true)
                .put("customerId", "1")
                .put("customerName", "John Doe")
                .put("totalMessages", 1)
                .toString();
        receiver.processMessage(jobStartMessage);

        // Now send job data message
        JSONArray charges = new JSONArray().put(new JSONObject().put("id", "1").put("kwh", 10));
        String jobDataMessage = new JSONObject()
                .put("customerId", "1")
                .put("charges", charges)
                .toString();

        // Act
        receiver.processMessage(jobDataMessage);

        // Assert
        assertEquals(1, DataCollectionReceiver.jobReceived.get("1"));
        assertEquals(convertJSONArrayToStringList(charges), convertJSONArrayToStringList(DataCollectionReceiver.jobData.get("1")));

        // Verify that the message was published
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(channel, times(1)).basicPublish(eq(""), eq("pdfGeneratorQueue"), eq(null), captor.capture());
        String sentMessage = new String(captor.getValue(), StandardCharsets.UTF_8);
        JSONObject sentJson = new JSONObject(sentMessage);
        assertEquals("1", sentJson.getString("customerId"));
        assertEquals("Customer: John Doe", sentJson.getString("customer"));
        assertEquals(convertJSONArrayToStringList(charges), convertJSONArrayToStringList(sentJson.getJSONArray("charges")));
    }

    private List<String> convertJSONArrayToStringList(JSONArray jsonArray) {
        return IntStream.range(0, jsonArray.length())
                .mapToObj(jsonArray::get)
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
