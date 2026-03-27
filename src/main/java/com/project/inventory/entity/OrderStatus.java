package com.project.inventory.entity;

/**
 * Enum for Order Status
 * - PENDING: Order placed, waiting for processing
 * - COMPLETED: Order processed and delivered
 * - CANCELLED: Order cancelled by user or admin
 */
public enum OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
