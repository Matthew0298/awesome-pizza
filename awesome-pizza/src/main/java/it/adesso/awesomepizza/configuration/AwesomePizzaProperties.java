package it.adesso.awesomepizza.configuration;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "awesome-pizza")
public record AwesomePizzaProperties(
        @Min(1) int maxPizzasPerOrder,
        @Min(1) int maxQuantityPerPizza
) {
}
