package it.adesso.awesomepizza.service;

import it.adesso.awesomepizza.configuration.AwesomePizzaProperties;
import it.adesso.awesomepizza.exception.InvalidOrderStateException;
import it.adesso.awesomepizza.exception.OrderNotFoundException;
import it.adesso.awesomepizza.model.Order;
import it.adesso.awesomepizza.model.OrderDTO;
import it.adesso.awesomepizza.model.OrderStatus;
import it.adesso.awesomepizza.model.Pizza;
import it.adesso.awesomepizza.repo.OrderRepository;
import it.adesso.awesomepizza.service.mapper.OrderMapper;
import it.adesso.awesomepizza.utils.logging.OrderLogUtils;
import it.adesso.awesomepizza.utils.validation.ValidateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("Creating new order with {} pizzas", orderDTO.getPizzas().size());
        validateOrderRequest(orderDTO);

        Order order = Order.builder()
                .status(OrderStatus.RECEIVED)
                .pizzas(orderDTO.getPizzas().stream()
                        .map(pizzaDTO -> Pizza.builder()
                                .name(pizzaDTO.getName())
                                .quantity(pizzaDTO.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // Set the order reference for each pizza
        order.getPizzas().forEach(pizza -> pizza.setOrder(order));

        Order savedOrder = orderRepository.save(order);
        OrderLogUtils.logOrderCreated(log, savedOrder.getCode(), savedOrder.getPizzas().size());

        return orderMapper.toDTO(savedOrder);
    }

    /**
     * Get order by code
     */
    @Transactional(readOnly = true)
    @Override
    public OrderDTO getOrderByCode(String code) {
        log.info("Retrieving order by code: {}", code);

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
        log.info("Retrieving order by id: {}", id);

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
        log.info("Retrieving all orders");

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
        log.info("Retrieving order queue");

        List<Order> queueOrders = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED);
        return queueOrders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Start order (change status to IN_PROGRESS)
     */
    @Override
    public OrderDTO startOrder(Long id) {
        log.info("Starting order with id: {}", id);

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
        OrderLogUtils.logStatusTransition(log, id, previousStatus, updatedOrder.getStatus());

        return orderMapper.toDTO(updatedOrder);
    }

    /**
     * Mark order as ready
     */
    @Override
    public OrderDTO markAsReady(Long id) {
        log.info("Marking order with id: {} as ready", id);

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
        OrderLogUtils.logStatusTransition(log, id, previousStatus, updatedOrder.getStatus());

        return orderMapper.toDTO(updatedOrder);
    }

    /**
     * Complete order (change status to COMPLETED)
     */
    @Override
    public OrderDTO completeOrder(Long id) {
        log.info("Completing order with id: {}", id);

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
        OrderLogUtils.logStatusTransition(log, id, previousStatus, updatedOrder.getStatus());

        return orderMapper.toDTO(updatedOrder);
    }

    private void validateOrderRequest(OrderDTO orderDTO) {
        ValidateUtils.requireNotNull(orderDTO, "Order payload cannot be null");
        ValidateUtils.requireNotNull(orderDTO.getPizzas(), "Order pizzas cannot be null");
        ValidateUtils.requireTrue(!orderDTO.getPizzas().isEmpty(), "Order must contain at least one pizza");
        ValidateUtils.requireMax(
                orderDTO.getPizzas().size(),
                properties.maxPizzasPerOrder(),
                "Order exceeds maximum pizzas allowed: " + properties.maxPizzasPerOrder()
        );
        orderDTO.getPizzas().forEach(pizzaDTO -> {
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

