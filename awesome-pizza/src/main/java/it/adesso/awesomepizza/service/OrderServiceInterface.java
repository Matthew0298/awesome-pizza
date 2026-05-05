package it.adesso.awesomepizza.service;

import it.adesso.awesomepizza.model.CreateOrderRequest;
import it.adesso.awesomepizza.model.OrderDTO;
import it.adesso.awesomepizza.model.OrderPriority;

import java.util.List;

public interface OrderServiceInterface {

    OrderDTO createOrder(CreateOrderRequest request);

    OrderDTO getOrderByCode(String code);

    OrderDTO getOrderById(Long id);

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrderQueue();

    OrderDTO startOrder(Long id);

    OrderDTO markAsReady(Long id);

    OrderDTO completeOrder(Long id);

    OrderDTO cancelOrder(Long id);

    OrderDTO updatePriority(Long id, OrderPriority priority);
}
