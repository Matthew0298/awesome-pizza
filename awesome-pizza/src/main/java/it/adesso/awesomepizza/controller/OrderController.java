package it.adesso.awesomepizza.controller;

import it.adesso.awesomepizza.constant.ApiPaths;
import it.adesso.awesomepizza.model.OrderDTO;
import it.adesso.awesomepizza.service.OrderServiceInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.ORDERS)
@Slf4j
public class OrderController {

    private final OrderServiceInterface orderService;

    public OrderController(OrderServiceInterface orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /orders - Create a new order
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        log.info("Request to create new order");
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * GET /orders/{code} - Get order by code
     */
    @GetMapping("/{code}")
    public ResponseEntity<OrderDTO> getOrderByCode(@PathVariable String code) {
        log.info("Request to get order by code: {}", code);
        OrderDTO orderDTO = orderService.getOrderByCode(code);
        return ResponseEntity.ok(orderDTO);
    }

    /**
     * GET /orders - Get all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        log.info("Request to get all orders");
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /orders/queue - Get order queue (RECEIVED orders)
     */
    @GetMapping("/queue/waiting")
    public ResponseEntity<List<OrderDTO>> getOrderQueue() {
        log.info("Request to get order queue");
        List<OrderDTO> queueOrders = orderService.getOrderQueue();
        return ResponseEntity.ok(queueOrders);
    }

    /**
     * PUT /orders/{id}/start - Start order (change to IN_PROGRESS)
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<OrderDTO> startOrder(@PathVariable Long id) {
        log.info("Request to start order with id: {}", id);
        OrderDTO startedOrder = orderService.startOrder(id);
        return ResponseEntity.ok(startedOrder);
    }

    /**
     * PUT /orders/{id}/ready - Mark order as ready
     */
    @PutMapping("/{id}/ready")
    public ResponseEntity<OrderDTO> markAsReady(@PathVariable Long id) {
        log.info("Request to mark order as ready with id: {}", id);
        OrderDTO readyOrder = orderService.markAsReady(id);
        return ResponseEntity.ok(readyOrder);
    }

    /**
     * PUT /orders/{id}/complete - Complete order (change to COMPLETED)
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<OrderDTO> completeOrder(@PathVariable Long id) {
        log.info("Request to complete order with id: {}", id);
        OrderDTO completedOrder = orderService.completeOrder(id);
        return ResponseEntity.ok(completedOrder);
    }
}

