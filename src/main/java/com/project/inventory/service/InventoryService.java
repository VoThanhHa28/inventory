package com.project.inventory.service;

import com.project.inventory.dto.inventory.InventoryRequestDTO;
import com.project.inventory.dto.inventory.InventoryResponseDTO;
import java.util.List;

/**
 * InventoryService - Business logic for inventory management
 * 
 * Handles:
 * - Adding/removing stock
 * - Reserving stock for orders
 * - Marking sold inventory
 * - Retrieving inventory details
 */
public interface InventoryService {

    /**
     * Add stock to inventory
     * @throws InvalidProductException if product doesn't exist
     * @throws NegativeQuantityException if quantity is negative
     */
    InventoryResponseDTO addStock(InventoryRequestDTO request);

    /**
     * Remove stock from inventory (admin operation)
     * @throws InvalidProductException if product doesn't exist
     * @throws NegativeQuantityException if quantity is negative
     * @throws InsufficientStockException if stock is insufficient
     */
    InventoryResponseDTO removeStock(InventoryRequestDTO request);

    /**
     * Reserve stock for an order (decrease stock, increase reserved)
     * @throws InvalidProductException if product doesn't exist
     * @throws NegativeQuantityException if quantity is negative
     * @throws InsufficientStockException if available stock is insufficient
     */
    InventoryResponseDTO reserve(InventoryRequestDTO request);

    /**
     * Mark reserved stock as sold (decrease reserved, increase soldCount)
     * @throws InvalidProductException if product doesn't exist
     * @throws NegativeQuantityException if quantity is negative
     * @throws InsufficientStockException if reserved stock is insufficient
     */
    InventoryResponseDTO markAsSold(InventoryRequestDTO request);

    /**
     * Release reserved stock back to available stock
     * @throws InvalidProductException if product doesn't exist
     * @throws NegativeQuantityException if quantity is negative
     * @throws InsufficientStockException if reserved stock is insufficient
     */
    InventoryResponseDTO releaseReserved(InventoryRequestDTO request);

    /**
     * Get all inventory with product details
     */
    List<InventoryResponseDTO> getAll();

    /**
     * Get inventory detail by ID with product info
     * @throws InvalidProductException if inventory not found
     */
    InventoryResponseDTO getById(Long inventoryId);

    /**
     * Get inventory by product ID
     * @throws InvalidProductException if product doesn't have inventory
     */
    InventoryResponseDTO getByProductId(Long productId);
}
