package it.adesso.awesomepizza.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;

    private String code;

    private OrderStatus status;

    /** Set by persistence / admin endpoints; never supplied on creation (use {@link CreateOrderRequest}). */
    private OrderPriority priority;

    private List<PizzaDTO> pizzas;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

