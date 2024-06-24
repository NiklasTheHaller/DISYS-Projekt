package org.example.invoiceapi;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class InvoiceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoiceApiApplication.class, args);
    }

}
