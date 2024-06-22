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
    private static final String OUTPUT_PATH = System.getProperty("user.home") + File.separator + "invoices" + File.separator;

    public static void main(String[] args) {
        RabbitMQConfig config = new RabbitMQConfig();
        try (Connection connection = config.createConnection();
             Channel channel = connection.createChannel()) {

            config.setupQueue(channel, INPUT_QUEUE);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String input = new String(delivery.getBody(), StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    System.out.println(" [x] Received Data: '" + input + "'");
                    // Ensure the directory exists
                    File directory = new File(OUTPUT_PATH);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    // Generate PDF
                    String outputPath = OUTPUT_PATH + "invoice_" + System.currentTimeMillis() + ".pdf";
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(outputPath));
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
                        JsonNode chargeNode = iterator.next();
                        // Parse the chargeNode as it is a string representing a JSON object
                        JsonNode charge = mapper.readTree(chargeNode.asText());
                        double kwh = charge.get("kwh").asDouble();
                        totalKwh += kwh;
                        document.add(new Paragraph("Charge ID: " + charge.get("id").asText() + ", kWh: " + kwh));
                    }

                    document.add(new Paragraph("\nTotal kWh: " + totalKwh));
                    document.close();
                    System.out.println(" [x] PDF generated and saved to: " + outputPath);
                } catch (DocumentException | IOException e) {
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
