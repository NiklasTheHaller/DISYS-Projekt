package org.example.invoiceapi.controller;

import org.example.invoiceapi.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/{customerId}")
    public ResponseEntity<String> generateInvoice(@PathVariable int customerId) {
        invoiceService.sendInvoiceRequest(customerId);
        return ResponseEntity.ok("Invoice generation started for customer ID: " + customerId);
    }
}
