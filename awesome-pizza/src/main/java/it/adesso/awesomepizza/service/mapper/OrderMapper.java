package it.adesso.awesomepizza.service.mapper;

import it.adesso.awesomepizza.model.Order;
import it.adesso.awesomepizza.model.OrderDTO;
import it.adesso.awesomepizza.model.Pizza;
import it.adesso.awesomepizza.model.PizzaDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }

        return OrderDTO.builder()
                .id(order.getId())
                .code(order.getCode())
                .status(order.getStatus())
                .pizzas(toPizzaDTOs(order.getPizzas()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return null;
        }

        Order order = Order.builder()
                .id(orderDTO.getId())
                .code(orderDTO.getCode())
                .status(orderDTO.getStatus())
                .createdAt(orderDTO.getCreatedAt())
                .updatedAt(orderDTO.getUpdatedAt())
                .build();

        // Map pizzas
        if (orderDTO.getPizzas() != null) {
            order.setPizzas(orderDTO.getPizzas().stream()
                    .map(pizzaDTO -> toPizzaEntity(pizzaDTO, order))
                    .collect(Collectors.toList()));
        }

        return order;
    }

    public PizzaDTO toPizzaDTO(Pizza pizza) {
        if (pizza == null) {
            return null;
        }

        return PizzaDTO.builder()
                .id(pizza.getId())
                .name(pizza.getName())
                .quantity(pizza.getQuantity())
                .build();
    }

    public Pizza toPizzaEntity(PizzaDTO pizzaDTO, Order order) {
        if (pizzaDTO == null) {
            return null;
        }

        return Pizza.builder()
                .id(pizzaDTO.getId())
                .name(pizzaDTO.getName())
                .quantity(pizzaDTO.getQuantity())
                .order(order)
                .build();
    }

    public List<PizzaDTO> toPizzaDTOs(List<Pizza> pizzas) {
        if (pizzas == null) {
            return null;
        }

        return pizzas.stream()
                .map(this::toPizzaDTO)
                .collect(Collectors.toList());
    }
}

