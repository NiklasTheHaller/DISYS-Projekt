package org.example.logic;

import com.itextpdf.text.DocumentException;
import com.rabbitmq.client.Channel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PDFGeneratorTest {

    private static final String TEST_OUTPUT_PATH = System.getProperty("user.home") + File.separator + "invoices" + File.separator + "test" + File.separator;
    private Channel channel;
    private PDFGenerator generator;

    @BeforeEach
    public void setUp() {
        channel = mock(Channel.class);
        generator = new PDFGenerator(channel, TEST_OUTPUT_PATH);

        // Clean the test output directory before running each test
        File outputDir = new File(TEST_OUTPUT_PATH);
        if (outputDir.exists()) {
            Arrays.stream(Objects.requireNonNull(outputDir.listFiles())).forEach(File::delete);
        } else {
            outputDir.mkdirs();
        }
    }

    @Test
    public void testProcessMessage() throws IOException, DocumentException {
        // Arrange
        long startTime = System.currentTimeMillis();
        JSONArray charges = new JSONArray()
                .put(new JSONObject().put("id", "1").put("kwh", 10).toString())
                .put(new JSONObject().put("id", "2").put("kwh", 20).toString());
        String input = new JSONObject()
                .put("customerId", 1)
                .put("customer", "Customer: John Doe")
                .put("startTime", startTime)
                .put("charges", charges)
                .toString();

        // Act
        generator.processMessage(input);

        // Assert
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(channel, times(1)).basicPublish(eq(""), eq("invoiceMetadataQueue"), eq(null), captor.capture());

        String sentMessage = new String(captor.getValue(), StandardCharsets.UTF_8);
        JSONObject sentJson = new JSONObject(sentMessage);

        assertEquals(1, sentJson.getInt("customerId"));
        assertTrue(sentJson.has("filePath"));
        assertTrue(sentJson.has("creationTime"));
        assertTrue(sentJson.has("totalTime"));

        // Verify the PDF was created
        File[] files = new File(TEST_OUTPUT_PATH).listFiles((dir, name) -> name.startsWith("invoice_") && name.endsWith(".pdf"));
        assert files != null;
        assertEquals(1, files.length);
        File pdfFile = new File(sentJson.getString("filePath"));
        assertTrue(pdfFile.exists());

        // Cleanup after test
        boolean isDeleted = pdfFile.delete();
        if (!isDeleted) {
            System.err.println("Failed to delete the file: " + pdfFile.getPath());
        }
    }
}
