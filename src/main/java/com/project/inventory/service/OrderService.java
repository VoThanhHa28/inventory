package com.project.inventory.service;

import com.project.inventory.dto.order.OrderRequestDTO;
import com.project.inventory.dto.order.OrderResponseDTO;

public interface OrderService {
    OrderResponseDTO placeOrder(OrderRequestDTO requestDTO);
}