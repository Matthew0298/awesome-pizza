package it.adesso.awesomepizza.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.adesso.awesomepizza.constant.ApiPaths;
import it.adesso.awesomepizza.model.OrderDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(ApiPaths.ORDERS)
@Tag(name = "Orders", description = "Operations for managing pizza orders")
public interface OrderApi {

    @PostMapping
    @Operation(summary = "Create order", description = "Creates a new order with one or more pizzas")
    ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO);

    @GetMapping("/{code}")
    @Operation(summary = "Get order by code", description = "Retrieves a single order using its public code")
    ResponseEntity<OrderDTO> getOrderByCode(@PathVariable String code);

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all orders sorted by creation time")
    ResponseEntity<List<OrderDTO>> getAllOrders();

    @GetMapping("/queue/waiting")
    @Operation(summary = "Get waiting queue", description = "Retrieves orders currently waiting in RECEIVED status")
    ResponseEntity<List<OrderDTO>> getOrderQueue();

    @PutMapping("/{id}/start")
    @Operation(summary = "Start order", description = "Moves order status from RECEIVED to IN_PROGRESS")
    ResponseEntity<OrderDTO> startOrder(@PathVariable Long id);

    @PutMapping("/{id}/ready")
    @Operation(summary = "Mark order ready", description = "Moves order status from IN_PROGRESS to READY")
    ResponseEntity<OrderDTO> markAsReady(@PathVariable Long id);

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete order", description = "Moves order status from READY to COMPLETED")
    ResponseEntity<OrderDTO> completeOrder(@PathVariable Long id);
}
