package it.adesso.awesomepizza.service;

import it.adesso.awesomepizza.model.OrderDTO;

import java.util.List;

public interface OrderServiceInterface {

    OrderDTO createOrder(OrderDTO orderDTO);

    OrderDTO getOrderByCode(String code);

    OrderDTO getOrderById(Long id);

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrderQueue();

    OrderDTO startOrder(Long id);

    OrderDTO markAsReady(Long id);

    OrderDTO completeOrder(Long id);
}
