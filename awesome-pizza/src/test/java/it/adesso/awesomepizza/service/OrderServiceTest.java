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
import it.adesso.awesomepizza.model.PizzaDTO;
import it.adesso.awesomepizza.repo.OrderRepository;
import it.adesso.awesomepizza.service.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private final AwesomePizzaProperties properties = new AwesomePizzaProperties(10, 20);

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private Pizza testPizza;
    private PizzaDTO testPizzaDTO;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderMapper, properties);
        // Setup test data
        testPizzaDTO = PizzaDTO.builder()
                .id(1L)
                .name("Margherita")
                .quantity(2)
                .build();

        testPizza = Pizza.builder()
                .id(1L)
                .name("Margherita")
                .quantity(2)
                .build();

        testOrderDTO = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.RECEIVED)
                .priority(OrderPriority.NORMAL)
                .pizzas(List.of(testPizzaDTO))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testOrder = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.RECEIVED)
                .priority(OrderPriority.NORMAL)
                .pizzas(new ArrayList<>(List.of(testPizza)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPizza.setOrder(testOrder);
    }

    @Test
    void testCreateOrder() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        CreateOrderRequest createRequest = CreateOrderRequest.builder()
                .pizzas(testOrderDTO.getPizzas())
                .build();

        OrderDTO result = orderService.createOrder(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("ABC12345", result.getCode());
        assertEquals(OrderStatus.RECEIVED, result.getStatus());
        assertEquals(1, result.getPizzas().size());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderMapper, times(1)).toDTO(testOrder);
    }

    @Test
    void testGetOrderByCode() {
        // Arrange
        when(orderRepository.findByCode("ABC12345")).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.getOrderByCode("ABC12345");

        // Assert
        assertNotNull(result);
        assertEquals("ABC12345", result.getCode());
        verify(orderRepository, times(1)).findByCode("ABC12345");
    }

    @Test
    void testGetOrderByCode_NotFound() {
        // Arrange
        when(orderRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderByCode("INVALID");
        });
        verify(orderRepository, times(1)).findByCode("INVALID");
    }

    @Test
    void testGetOrderById() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });
        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAllOrders() {
        // Arrange
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(orders);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        List<OrderDTO> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void testGetOrderQueue() {
        // Arrange
        Order highPriorityOlder = Order.builder()
                .id(2L)
                .code("DEF67890")
                .status(OrderStatus.RECEIVED)
                .priority(OrderPriority.HIGH)
                .pizzas(new ArrayList<>(List.of(testPizza)))
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .updatedAt(LocalDateTime.now())
                .build();
        List<Order> queueOrders = List.of(testOrder, highPriorityOlder);
        when(orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED))
                .thenReturn(queueOrders);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
        when(orderMapper.toDTO(highPriorityOlder)).thenReturn(
                OrderDTO.builder()
                        .id(2L)
                        .code("DEF67890")
                        .status(OrderStatus.RECEIVED)
                        .priority(OrderPriority.HIGH)
                        .build()
        );

        // Act
        List<OrderDTO> result = orderService.getOrderQueue();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("DEF67890", result.get(0).getCode());
        verify(orderRepository, times(1)).findByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED);
    }

    @Test
    void testStartOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        Order startedOrder = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.IN_PROGRESS)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(startedOrder);
        OrderDTO startedOrderDTO = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.IN_PROGRESS)
                .build();
        when(orderMapper.toDTO(startedOrder)).thenReturn(startedOrderDTO);

        // Act
        OrderDTO result = orderService.startOrder(1L);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.IN_PROGRESS, result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testStartOrder_NotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.startOrder(999L);
        });
    }

    @Test
    void testCompleteOrder() {
        // Arrange
        Order readyOrder = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.READY)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(readyOrder));
        Order completedOrder = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.COMPLETED)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(completedOrder);
        OrderDTO completedOrderDTO = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.COMPLETED)
                .build();
        when(orderMapper.toDTO(completedOrder)).thenReturn(completedOrderDTO);

        // Act
        OrderDTO result = orderService.completeOrder(1L);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCompleteOrder_NotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.completeOrder(999L);
        });
    }

    @Test
    void testCancelOrder_FromReceived() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        Order cancelled = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.CANCELLED)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(cancelled);
        OrderDTO cancelledDto = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.CANCELLED)
                .build();
        when(orderMapper.toDTO(cancelled)).thenReturn(cancelledDto);

        OrderDTO result = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCancelOrder_FromInProgress() {
        Order inProgress = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.IN_PROGRESS)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(inProgress));
        Order cancelled = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.CANCELLED)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(cancelled);
        OrderDTO cancelledDto = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.CANCELLED)
                .build();
        when(orderMapper.toDTO(cancelled)).thenReturn(cancelledDto);

        OrderDTO result = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCancelOrder_IdempotentWhenAlreadyCancelled() {
        Order cancelled = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.CANCELLED)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelled));
        OrderDTO cancelledDto = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.CANCELLED)
                .build();
        when(orderMapper.toDTO(cancelled)).thenReturn(cancelledDto);

        OrderDTO result = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_RejectedWhenReady() {
        Order ready = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.READY)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ready));

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(1L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(999L));
    }

    @Test
    void testUpdatePriority_ForReceivedOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        Order updated = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.RECEIVED)
                .priority(OrderPriority.HIGH)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(updated);
        when(orderMapper.toDTO(updated)).thenReturn(OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.RECEIVED)
                .priority(OrderPriority.HIGH)
                .build());

        OrderDTO result = orderService.updatePriority(1L, OrderPriority.HIGH);

        assertEquals(OrderPriority.HIGH, result.getPriority());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdatePriority_RejectsNonReceivedOrder() {
        Order inProgress = Order.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.IN_PROGRESS)
                .priority(OrderPriority.NORMAL)
                .pizzas(testOrder.getPizzas())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(inProgress));

        assertThrows(InvalidOrderStateException.class, () -> orderService.updatePriority(1L, OrderPriority.HIGH));
        verify(orderRepository, never()).save(any(Order.class));
    }
}

