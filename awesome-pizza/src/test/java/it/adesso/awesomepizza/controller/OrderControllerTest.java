package it.adesso.awesomepizza.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.adesso.awesomepizza.constant.ApiPaths;
import it.adesso.awesomepizza.exception.GlobalExceptionHandler;
import it.adesso.awesomepizza.exception.InvalidOrderStateException;
import it.adesso.awesomepizza.exception.OrderNotFoundException;
import it.adesso.awesomepizza.model.CreateOrderRequest;
import it.adesso.awesomepizza.model.OrderDTO;
import it.adesso.awesomepizza.model.OrderPriority;
import it.adesso.awesomepizza.model.OrderStatus;
import it.adesso.awesomepizza.model.PizzaDTO;
import it.adesso.awesomepizza.model.UpdateOrderPriorityRequest;
import it.adesso.awesomepizza.service.OrderServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderServiceInterface orderService = mock(OrderServiceInterface.class);

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateOrder() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .pizzas(List.of(PizzaDTO.builder().name("Margherita").quantity(2).build()))
                .build();

        OrderDTO response = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.RECEIVED)
                .priority(OrderPriority.NORMAL)
                .pizzas(request.getPizzas())
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post(ApiPaths.ORDERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ABC12345"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void shouldRejectInvalidOrderPayload() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder().build();

        mockMvc.perform(post(ApiPaths.ORDERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        OrderDTO response = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.CANCELLED)
                .priority(OrderPriority.NORMAL)
                .build();

        when(orderService.cancelOrder(1L)).thenReturn(response);

        mockMvc.perform(put(ApiPaths.ORDERS + "/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC12345"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturnConflictWhenCancelNotAllowed() throws Exception {
        when(orderService.cancelOrder(1L)).thenThrow(
                new InvalidOrderStateException("Order cannot be cancelled. Current status: READY"));

        mockMvc.perform(put(ApiPaths.ORDERS + "/1/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Order cannot be cancelled. Current status: READY"));
    }

    @Test
    void shouldReturnNotFoundWhenCancelUnknownOrder() throws Exception {
        when(orderService.cancelOrder(999L)).thenThrow(new OrderNotFoundException("Order not found with id: 999"));

        mockMvc.perform(put(ApiPaths.ORDERS + "/999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"));
    }

    @Test
    void shouldUpdatePriorityWhenAdmin() throws Exception {
        UpdateOrderPriorityRequest request = UpdateOrderPriorityRequest.builder()
                .priority(OrderPriority.HIGH)
                .build();
        OrderDTO response = OrderDTO.builder()
                .id(1L)
                .code("ABC12345")
                .status(OrderStatus.RECEIVED)
                .priority(OrderPriority.HIGH)
                .build();

        when(orderService.updatePriority(eq(1L), eq(OrderPriority.HIGH))).thenReturn(response);

        mockMvc.perform(put(ApiPaths.ORDERS + "/1/priority")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void shouldRejectPriorityUpdateWhenNotAdmin() throws Exception {
        UpdateOrderPriorityRequest request = UpdateOrderPriorityRequest.builder()
                .priority(OrderPriority.HIGH)
                .build();

        mockMvc.perform(put(ApiPaths.ORDERS + "/1/priority")
                        .header("X-User-Role", "STAFF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only ADMIN can update order priority"));
    }
}
