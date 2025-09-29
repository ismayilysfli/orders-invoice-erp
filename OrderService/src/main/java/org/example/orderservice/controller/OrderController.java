package org.example.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.OrderRequest;
import org.example.orderservice.dto.OrderResponse;
import org.example.orderservice.service.OrderService;
import org.example.orderservice.service.PDFService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final PDFService pdfService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<FileSystemResource> downloadInvoice(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        String path = pdfService.generateInvoicePdf(order);
        orderService.recordInvoiceGenerated(order, path);
        File file = new File(path);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", file.getName());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
