package com.project.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // Do not use 'order' - it conflicts with SQL reserved keyword
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    // Order status: PENDING, COMPLETED, CANCELLED
    @Column(name = "status", nullable = false)
    private String status;

    // One-to-Many relationship with OrderDetail (1 Order has many items)
    // CascadeType.ALL ensures OrderDetails are persisted when Order is saved
    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();
}