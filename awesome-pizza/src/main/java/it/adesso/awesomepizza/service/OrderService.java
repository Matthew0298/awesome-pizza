package it.adesso.awesomepizza.service;

import it.adesso.awesomepizza.configuration.AwesomePizzaProperties;
import it.adesso.awesomepizza.exception.InvalidOrderStateException;
import it.adesso.awesomepizza.exception.OrderNotFoundException;
import it.adesso.awesomepizza.model.CreateOrderRequest;
import it.adesso.awesomepizza.model.Order;
import it.adesso.awesomepizza.model.OrderDTO;
import it.adesso.awesomepizza.model.OrderPriority;
import it.adesso.awesomepizza.model.OrderStatus;
import it.adesso.awesomepizza.model.Pizza;
import it.adesso.awesomepizza.repo.OrderRepository;
import it.adesso.awesomepizza.service.mapper.OrderMapper;
import it.adesso.awesomepizza.utils.logging.OrderLogUtils;
import it.adesso.awesomepizza.utils.validation.ValidateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final AwesomePizzaProperties properties;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, AwesomePizzaProperties properties) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.properties = properties;
    }

    /**
     * Create a new order with the given pizzas
     */
    @Override
    public OrderDTO createOrder(CreateOrderRequest request) {
        int pizzasCount = request != null && request.getPizzas() != null ? request.getPizzas().size() : 0;
        OrderLogUtils.logBusinessEvent(log, "order.create.requested", null, null, OrderStatus.RECEIVED, "pizzasCount=" + pizzasCount);
        validateCreateOrderRequest(request);

        Order order = Order.builder()
                .status(OrderStatus.RECEIVED)
                .priority(OrderPriority.NORMAL)
                .pizzas(request.getPizzas().stream()
                        .map(pizzaDTO -> Pizza.builder()
                                .name(pizzaDTO.getName())
                                .quantity(pizzaDTO.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // Set the order reference for each pizza
        order.getPizzas().forEach(pizza -> pizza.setOrder(order));

        Order savedOrder = orderRepository.save(order);
        OrderLogUtils.logOrderCreated(log, savedOrder.getId(), savedOrder.getCode(), savedOrder.getStatus(), savedOrder.getPizzas().size());

        return orderMapper.toDTO(savedOrder);
    }

    /**
     * Get order by code
     */
    @Transactional(readOnly = true)
    @Override
    public OrderDTO getOrderByCode(String code) {
        OrderLogUtils.logBusinessEvent(log, "order.get-by-code.requested", null, code, null, "");

        Order order = orderRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.error("Order not found with code: {}", code);
                    return new OrderNotFoundException("Order not found with code: " + code);
                });

        return orderMapper.toDTO(order);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    @Override
    public OrderDTO getOrderById(Long id) {
        OrderLogUtils.logBusinessEvent(log, "order.get-by-id.requested", id, null, null, "");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new OrderNotFoundException("Order not found with id: " + id);
                });

        return orderMapper.toDTO(order);
    }

    /**
     * Get all orders sorted by creation date (newest first)
     */
    @Transactional(readOnly = true)
    @Override
    public List<OrderDTO> getAllOrders() {
        OrderLogUtils.logBusinessEvent(log, "order.get-all.requested", null, null, null, "");

        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get queue of orders waiting (RECEIVED status)
     */
    @Transactional(readOnly = true)
    @Override
    public List<OrderDTO> getOrderQueue() {
        OrderLogUtils.logBusinessEvent(log, "order.queue.requested", null, null, OrderStatus.RECEIVED, "");

        List<Order> queueOrders = new ArrayList<>(orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED));
        queueOrders.sort(
                Comparator.comparingInt((Order order) -> priorityRank(order.getPriority())).reversed()
                        .thenComparing(Order::getCreatedAt)
        );
        return queueOrders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Start order (change status to IN_PROGRESS)
     */
    @Override
    public OrderDTO startOrder(Long id) {
        OrderLogUtils.logBusinessEvent(log, "order.start.requested", id, null, OrderStatus.RECEIVED, "");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new OrderNotFoundException("Order not found with id: " + id);
                });

        if (!order.getStatus().equals(OrderStatus.RECEIVED)) {
            log.warn("Cannot start order with id: {} - Current status: {}", id, order.getStatus());
            throw new InvalidOrderStateException("Order cannot be started. Current status: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.IN_PROGRESS);
        Order updatedOrder = orderRepository.save(order);
        OrderLogUtils.logStatusTransition(log, updatedOrder.getId(), updatedOrder.getCode(), previousStatus, updatedOrder.getStatus());

        return orderMapper.toDTO(updatedOrder);
    }

    /**
     * Mark order as ready
     */
    @Override
    public OrderDTO markAsReady(Long id) {
        OrderLogUtils.logBusinessEvent(log, "order.ready.requested", id, null, OrderStatus.IN_PROGRESS, "");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new OrderNotFoundException("Order not found with id: " + id);
                });

        if (!order.getStatus().equals(OrderStatus.IN_PROGRESS)) {
            log.warn("Cannot mark order as ready with id: {} - Current status: {}", id, order.getStatus());
            throw new InvalidOrderStateException("Order must be IN_PROGRESS to be marked as ready. Current status: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.READY);
        Order updatedOrder = orderRepository.save(order);
        OrderLogUtils.logStatusTransition(log, updatedOrder.getId(), updatedOrder.getCode(), previousStatus, updatedOrder.getStatus());

        return orderMapper.toDTO(updatedOrder);
    }

    /**
     * Complete order (change status to COMPLETED)
     */
    @Override
    public OrderDTO completeOrder(Long id) {
        OrderLogUtils.logBusinessEvent(log, "order.complete.requested", id, null, OrderStatus.READY, "");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new OrderNotFoundException("Order not found with id: " + id);
                });

        if (!order.getStatus().equals(OrderStatus.READY)) {
            log.warn("Cannot complete order with id: {} - Current status: {}", id, order.getStatus());
            throw new InvalidOrderStateException("Order must be READY to be completed. Current status: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        Order updatedOrder = orderRepository.save(order);
        OrderLogUtils.logStatusTransition(log, updatedOrder.getId(), updatedOrder.getCode(), previousStatus, updatedOrder.getStatus());

        return orderMapper.toDTO(updatedOrder);
    }

    /**
     * Cancel order (RECEIVED or IN_PROGRESS). Already CANCELLED is a no-op (idempotent).
     */
    @Override
    public OrderDTO cancelOrder(Long id) {
        OrderLogUtils.logBusinessEvent(log, "order.cancel.requested", id, null, null, "");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new OrderNotFoundException("Order not found with id: " + id);
                });

        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            return orderMapper.toDTO(order);
        }

        if (!order.getStatus().equals(OrderStatus.RECEIVED)
                && !order.getStatus().equals(OrderStatus.IN_PROGRESS)) {
            log.warn("Cannot cancel order with id: {} - Current status: {}", id, order.getStatus());
            throw new InvalidOrderStateException("Order cannot be cancelled. Current status: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        OrderLogUtils.logStatusTransition(log, updatedOrder.getId(), updatedOrder.getCode(), previousStatus, updatedOrder.getStatus());

        return orderMapper.toDTO(updatedOrder);
    }

    @Override
    public OrderDTO updatePriority(Long id, OrderPriority priority) {
        OrderLogUtils.logBusinessEvent(log, "order.priority.update.requested", id, null, null, "priority=" + priority);
        ValidateUtils.requireNotNull(priority, "Priority cannot be null");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new OrderNotFoundException("Order not found with id: " + id);
                });

        if (!order.getStatus().equals(OrderStatus.RECEIVED)) {
            throw new InvalidOrderStateException("Priority can be updated only for RECEIVED orders. Current status: " + order.getStatus());
        }

        order.setPriority(priority);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDTO(updatedOrder);
    }

    private int priorityRank(OrderPriority priority) {
        if (priority == null) {
            return OrderPriority.NORMAL.ordinal();
        }
        return priority.ordinal();
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        ValidateUtils.requireNotNull(request, "Order payload cannot be null");
        ValidateUtils.requireNotNull(request.getPizzas(), "Order pizzas cannot be null");
        ValidateUtils.requireTrue(!request.getPizzas().isEmpty(), "Order must contain at least one pizza");
        ValidateUtils.requireMax(
                request.getPizzas().size(),
                properties.maxPizzasPerOrder(),
                "Order exceeds maximum pizzas allowed: " + properties.maxPizzasPerOrder()
        );
        request.getPizzas().forEach(pizzaDTO -> {
            ValidateUtils.requireNotNull(pizzaDTO, "Pizza item cannot be null");
            ValidateUtils.requireNotBlank(pizzaDTO.getName(), "Pizza name cannot be blank");
            ValidateUtils.requireNotNull(pizzaDTO.getQuantity(), "Pizza quantity cannot be null");
            ValidateUtils.requireTrue(pizzaDTO.getQuantity() > 0, "Pizza quantity must be greater than 0");
            ValidateUtils.requireMax(
                    pizzaDTO.getQuantity(),
                    properties.maxQuantityPerPizza(),
                    "Pizza quantity exceeds maximum allowed: " + properties.maxQuantityPerPizza()
            );
        });
    }
}

