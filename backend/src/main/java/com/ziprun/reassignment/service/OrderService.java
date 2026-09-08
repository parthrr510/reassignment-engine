package com.ziprun.reassignment.service;

import com.ziprun.reassignment.domain.Order;
import com.ziprun.reassignment.domain.enums.OrderStatus;
import com.ziprun.reassignment.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> getActiveOrdersForAgent(String agentId) {
        return orderRepository.findByAssignedAgentIdAndStatus(agentId, OrderStatus.ASSIGNED);
    }
}
