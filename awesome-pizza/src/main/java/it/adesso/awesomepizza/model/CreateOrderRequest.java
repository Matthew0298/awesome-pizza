package it.adesso.awesomepizza.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @Valid
    @NotEmpty(message = "At least one pizza must be provided")
    private List<PizzaDTO> pizzas;
}
