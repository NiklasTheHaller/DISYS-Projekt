package org.example.logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.rabbitmq.client.*;
import org.example.config.RabbitMQConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class PDFGenerator {

    private static final String INPUT_QUEUE = "pdfGeneratorQueue";
    private static final String OUTPUT_QUEUE = "invoiceMetadataQueue";
    private static final String DEFAULT_OUTPUT_PATH = System.getProperty("user.home") + File.separator + "invoices" + File.separator;
    private final Channel channel;
    private final String outputPath;

    public PDFGenerator(Channel channel) {
        this(channel, DEFAULT_OUTPUT_PATH);
    }

    public PDFGenerator(Channel channel, String outputPath) {
        this.channel = channel;
        this.outputPath = outputPath;
    }

    public void processMessage(String input) throws IOException, DocumentException {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(" [x] Received Data: '" + input + "'");

        // Ensure the directory exists
        File directory = new File(outputPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate PDF
        String filePath = outputPath + "invoice_" + System.currentTimeMillis() + ".pdf";
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        document.add(new Paragraph("Invoice"));

        JsonNode rootNode = mapper.readTree(input);
        String customerInfo = rootNode.get("customer").asText();
        document.add(new Paragraph(customerInfo));
        document.add(new Paragraph("\nDetails:\n"));

        JsonNode chargesNode = rootNode.get("charges");
        Iterator<JsonNode> iterator = chargesNode.iterator();
        double totalKwh = 0.0;

        while (iterator.hasNext()) {
            JsonNode chargeNode = mapper.readTree(iterator.next().asText());
            double kwh = chargeNode.get("kwh").asDouble();
            totalKwh += kwh;
            document.add(new Paragraph("Charge ID: " + chargeNode.get("id").asText() + ", kWh: " + kwh));
        }

        document.add(new Paragraph("\nTotal kWh: " + totalKwh));
        document.close();
        System.out.println(" [x] PDF generated and saved to: " + filePath);

        // Extract customerId from the rootNode
        int customerId = rootNode.get("customerId").asInt();

        // Send metadata back to InvoiceAPI
        JsonNode metadata = mapper.createObjectNode()
                .put("customerId", customerId)
                .put("filePath", filePath)
                .put("creationTime", System.currentTimeMillis());
        channel.basicPublish("", OUTPUT_QUEUE, null, metadata.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent Metadata: '" + metadata + "'");
    }

    public static void main(String[] args) {
        RabbitMQConfig config = new RabbitMQConfig();
        try (Connection connection = config.createConnection();
             Channel channel = connection.createChannel()) {

            config.setupQueue(channel, INPUT_QUEUE);
            config.setupQueue(channel, OUTPUT_QUEUE);

            PDFGenerator generator = new PDFGenerator(channel);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String input = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    generator.processMessage(input);
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
