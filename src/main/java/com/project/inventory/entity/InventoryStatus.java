package com.project.inventory.entity;

/**
 * Enum for Inventory status
 * - AVAILABLE: Stock is available for purchase
 * - RESERVED: Stock is reserved for an order
 * - SOLD: Stock has been sold and soldCount increased
 */
public enum InventoryStatus {
    AVAILABLE,
    RESERVED,
    SOLD
}
