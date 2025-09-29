package org.example.orderservice.service;

import jakarta.transaction.Transactional;
import org.example.orderservice.dto.OrderItemRequest;
import org.example.orderservice.dto.OrderItemResponse;
import org.example.orderservice.dto.OrderRequest;
import org.example.orderservice.dto.OrderResponse;
import org.example.orderservice.exception.InvalidOrderException;
import org.example.orderservice.exception.OrderNotFoundException;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderItem;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final String inventoryBaseUrl;

    public OrderService(OrderRepository orderRepository,
                        RestTemplate restTemplate,
                        @Value("${inventory.base-url}") String inventoryBaseUrl) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.inventoryBaseUrl = inventoryBaseUrl;
    }

    public OrderResponse createOrder(OrderRequest orderRequest) {
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(orderRequest.getCustomerId());
        order.setStatus(OrderStatus.PENDING);

        // Decrement stock for each item first; if any fails, the whole call fails
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            decrementStock(itemRequest.getProductId(), itemRequest.getQuantity());
            BigDecimal price = getMockPrice(itemRequest.getProductId());
            OrderItem item = new OrderItem(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity(),
                    price
            );
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        return convertToResponse(savedOrder);
    }

    private void decrementStock(Long productId, int qty) {
        try {
            String url = inventoryBaseUrl + "/api/products/{id}/decrement?qty={qty}";
            ResponseEntity<Void> res = restTemplate.postForEntity(url, null, Void.class, productId, qty);
        } catch (HttpClientErrorException e) {
            String msg = e.getResponseBodyAsString();
            throw new InvalidOrderException("Failed to reserve stock for product " + productId + ": " + msg);
        }
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return convertToResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException("Only pending orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal getMockPrice(Long productId) {
        return BigDecimal.valueOf(9.99);
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus());

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                ))
                .toList();

        response.setItems(itemResponses);
        return response;
    }
}