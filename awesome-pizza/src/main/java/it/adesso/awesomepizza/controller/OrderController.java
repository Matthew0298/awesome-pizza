package it.adesso.awesomepizza.controller;

import it.adesso.awesomepizza.controller.api.OrderApi;
import it.adesso.awesomepizza.model.OrderDTO;
import it.adesso.awesomepizza.service.OrderServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class OrderController implements OrderApi {

    private final OrderServiceInterface orderService;

    public OrderController(OrderServiceInterface orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /orders - Create a new order
     */
    @Override
    public ResponseEntity<OrderDTO> createOrder(OrderDTO orderDTO) {
        log.info("Request to create new order");
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * GET /orders/{code} - Get order by code
     */
    @Override
    public ResponseEntity<OrderDTO> getOrderByCode(String code) {
        log.info("Request to get order by code: {}", code);
        OrderDTO orderDTO = orderService.getOrderByCode(code);
        return ResponseEntity.ok(orderDTO);
    }

    /**
     * GET /orders - Get all orders
     */
    @Override
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        log.info("Request to get all orders");
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /orders/queue - Get order queue (RECEIVED orders)
     */
    @Override
    public ResponseEntity<List<OrderDTO>> getOrderQueue() {
        log.info("Request to get order queue");
        List<OrderDTO> queueOrders = orderService.getOrderQueue();
        return ResponseEntity.ok(queueOrders);
    }

    /**
     * PUT /orders/{id}/start - Start order (change to IN_PROGRESS)
     */
    @Override
    public ResponseEntity<OrderDTO> startOrder(Long id) {
        log.info("Request to start order with id: {}", id);
        OrderDTO startedOrder = orderService.startOrder(id);
        return ResponseEntity.ok(startedOrder);
    }

    /**
     * PUT /orders/{id}/ready - Mark order as ready
     */
    @Override
    public ResponseEntity<OrderDTO> markAsReady(Long id) {
        log.info("Request to mark order as ready with id: {}", id);
        OrderDTO readyOrder = orderService.markAsReady(id);
        return ResponseEntity.ok(readyOrder);
    }

    /**
     * PUT /orders/{id}/complete - Complete order (change to COMPLETED)
     */
    @Override
    public ResponseEntity<OrderDTO> completeOrder(Long id) {
        log.info("Request to complete order with id: {}", id);
        OrderDTO completedOrder = orderService.completeOrder(id);
        return ResponseEntity.ok(completedOrder);
    }
}

