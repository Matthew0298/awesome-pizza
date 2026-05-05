package it.adesso.awesomepizza.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderPriorityRequest {

    @NotNull(message = "Priority is required")
    private OrderPriority priority;
}
