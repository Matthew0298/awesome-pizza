package it.adesso.awesomepizza.application.dto;

import it.adesso.awesomepizza.domain.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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

    @Valid
    @NotEmpty(message = "At least one pizza must be provided")
    private List<PizzaDTO> pizzas;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

