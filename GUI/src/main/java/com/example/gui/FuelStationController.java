package com.example.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Content;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class FuelStationController {

    @FXML
    private TextField customerIdField;

    @FXML
    private Button generateInvoiceButton;
    @FXML
    private Button getInvoiceButton;

    @FXML
    private Label statusLabel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    long currentTime;

    @FXML
    private void generateInvoice() {
        currentTime = System.currentTimeMillis();
        String customerId = customerIdField.getText();
        if (customerId == null || customerId.isEmpty()) {
            statusLabel.setText("Customer ID cannot be empty");
            return;
        }

        generateInvoiceButton.setDisable(true);
        statusLabel.setText("Generating invoice...");

        new Thread(() -> {
            try {
                // Start data gathering job
                Content responseContent = Request.post("http://localhost:8080/invoices/" + customerId)
                        .execute().returnContent();

                if (responseContent != null) {
                    Thread.sleep(1000);
                    checkInvoiceStatus(customerId);
                } else {
                    Platform.runLater(() -> statusLabel.setText("Failed to start invoice generation"));
                }
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                Platform.runLater(() -> generateInvoiceButton.setDisable(false));
            }
        }).start();
    }

    private void checkInvoiceStatus(String customerId) {
        new Thread(() -> {
            while (true) {
                try {
                    Content responseContent = Request.get("http://localhost:8080/invoices/" + customerId)
                            .execute().returnContent();

                    if (responseContent != null) {
                        JsonNode jsonNode = objectMapper.readTree(responseContent.asString());

                        // Log the response for debugging
                        System.out.println("Received JSON response: " + jsonNode.toString());

                        String downloadLink = jsonNode.get("downloadLink").asText();
                        JsonNode creationTimeNode = jsonNode.get("creationTime");
                        JsonNode totalTimeNode = jsonNode.get("totalTime");

                        if (creationTimeNode != null && totalTimeNode != null) {
                            long creationTime = creationTimeNode.asLong();
                            long totalTime = totalTimeNode.asLong();
                            Platform.runLater(() -> statusLabel.setText("Invoice generated: " + downloadLink + " (File generated in " + totalTime + "ms)"));
                        } else {
                            Platform.runLater(() -> statusLabel.setText("Invoice generated: " + downloadLink));
                        }
                        break;
                    } else {
                        Thread.sleep(5000); // Wait for 5 seconds before checking again
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
                    break;
                }
            }
        }).start();
    }

    @FXML
    private void getInvoice() {
        String customerId = customerIdField.getText();
        if (customerId == null || customerId.isEmpty()) {
            statusLabel.setText("Customer ID cannot be empty");
            return;
        }

        getInvoiceButton.setDisable(true);
        statusLabel.setText("Fetching invoice...");

        new Thread(() -> {
            try {
                Content responseContent = Request.get("http://localhost:8080/invoices/" + customerId)
                        .execute().returnContent();

                Platform.runLater(() -> {
                    if (responseContent != null) {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(responseContent.asString());

                            // Log the response for debugging
                            System.out.println("Received JSON response: " + jsonNode.toString());

                            String downloadLink = jsonNode.get("downloadLink").asText();
                            JsonNode totalTimeNode = jsonNode.get("totalTime");

                            if (totalTimeNode != null) {
                                long totalTime = totalTimeNode.asLong();
                                statusLabel.setText("Invoice available: " + downloadLink + " (File generated in " + totalTime + "ms)");
                            } else {
                                statusLabel.setText("Invoice available: " + downloadLink);
                            }
                        } catch (IOException e) {
                            statusLabel.setText("Error: " + e.getMessage());
                        }
                    } else {
                        statusLabel.setText("Invoice not found");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> getInvoiceButton.setDisable(false));
            }
        }).start();
    }
}
