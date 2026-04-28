package it.adesso.awesomepizza.repository;

import it.adesso.awesomepizza.repository.entity.Order;
import it.adesso.awesomepizza.application.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByCode(String code);

    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    List<Order> findAllByOrderByCreatedAtDesc();
}

