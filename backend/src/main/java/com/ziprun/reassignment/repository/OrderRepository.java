package com.ziprun.reassignment.repository;

import com.ziprun.reassignment.domain.Order;
import com.ziprun.reassignment.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByAssignedAgentIdAndStatus(String assignedAgentId, OrderStatus status);
}
