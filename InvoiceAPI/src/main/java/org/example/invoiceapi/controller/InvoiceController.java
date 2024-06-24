package org.example.invoiceapi.controller;

import org.example.invoiceapi.model.InvoiceMetadata;
import org.example.invoiceapi.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/{customerId}")
    public ResponseEntity<String> generateInvoice(@PathVariable int customerId) {
        long startTime = System.currentTimeMillis();
        invoiceService.sendInvoiceRequest(customerId, startTime);
        return ResponseEntity.ok("Invoice generation started for customer ID: " + customerId);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> getInvoice(@PathVariable int customerId) {
        Optional<InvoiceMetadata> metadata = invoiceService.getInvoiceMetadata(customerId);
        if (metadata.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("downloadLink", metadata.get().getDownloadLink());
            response.put("creationTime", metadata.get().getCreationTime());
            response.put("totalTime", metadata.get().getTotalTime());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }
}
