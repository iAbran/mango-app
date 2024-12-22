package com.motete.mango.ecommerce_backend.service;

import com.motete.mango.ecommerce_backend.model.LocalUser;
import com.motete.mango.ecommerce_backend.model.WebOrder;
import com.motete.mango.ecommerce_backend.repository.WebOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final WebOrderRepository orderRepository;

    public OrderService(WebOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<WebOrder> getOrders(LocalUser user) {
        return orderRepository.findByUser(user);
    }
}
