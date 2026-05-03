package it.adesso.awesomepizza.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PizzaDTO {

    private Long id;

    @NotBlank(message = "Pizza name cannot be blank")
    private String name;

    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
}

