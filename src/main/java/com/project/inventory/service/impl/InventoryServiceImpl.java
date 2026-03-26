package com.project.inventory.service.impl;

import com.project.inventory.dto.inventory.InventoryRequestDTO;
import com.project.inventory.dto.inventory.InventoryResponseDTO;
import com.project.inventory.entity.Inventory;
import com.project.inventory.entity.InventoryStatus;
import com.project.inventory.exception.InsufficientStockException;
import com.project.inventory.exception.InvalidProductException;
import com.project.inventory.exception.NegativeQuantityException;
import com.project.inventory.repository.InventoryRepository;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * InventoryServiceImpl - Atomic inventory management implementation
 * 
 * All operations are @Transactional with native SQL for atomicity guarantee
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public InventoryResponseDTO addStock(InventoryRequestDTO request) {
        log.info("Adding {} stock to product {}", request.getQuantity(), request.getProductId());
        
        validateRequest(request);

        // Ensure inventory exists or create it
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseGet(() -> {
                    var product = productRepository.findById(request.getProductId())
                            .orElseThrow(() -> new InvalidProductException("Product not found: " + request.getProductId()));
                    
                    Inventory newInventory = Inventory.builder()
                            .product(product)
                            .stock(0)
                            .reserved(0)
                            .soldCount(0)
                            .status(InventoryStatus.AVAILABLE)
                            .build();
                    return inventoryRepository.save(newInventory);
                });

        // Atomic operation
        int result = inventoryRepository.addStock(request.getProductId(), request.getQuantity());
        if (result == 0) {
            throw new InvalidProductException("Failed to add stock - product not found");
        }

        inventory = inventoryRepository.findByProductId(request.getProductId()).get();
        log.info("Stock added successfully. Current stock: {}", inventory.getStock());
        
        return mapToDTO(inventory);
    }

    @Override
    @Transactional
    public InventoryResponseDTO removeStock(InventoryRequestDTO request) {
        log.info("Removing {} stock from product {}", request.getQuantity(), request.getProductId());
        
        validateRequest(request);

        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new InvalidProductException("Product not found: " + request.getProductId()));

        if (inventory.getStock() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock. Available: " + inventory.getStock());
        }

        // Atomic operation
        int result = inventoryRepository.removeStock(request.getProductId(), request.getQuantity());
        if (result == 0) {
            throw new InsufficientStockException("Failed to remove stock - insufficient stock");
        }

        inventory = inventoryRepository.findByProductId(request.getProductId()).get();
        log.info("Stock removed successfully. Current stock: {}", inventory.getStock());
        
        return mapToDTO(inventory);
    }

    @Override
    @Transactional
    public InventoryResponseDTO reserve(InventoryRequestDTO request) {
        log.info("Reserving {} stock for product {}", request.getQuantity(), request.getProductId());
        
        validateRequest(request);

        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new InvalidProductException("Product not found: " + request.getProductId()));

        if (!inventory.hasSufficientStock(request.getQuantity())) {
            throw new InsufficientStockException("Insufficient available stock. Available: " + inventory.getAvailable());
        }

        // Atomic operation
        int result = inventoryRepository.reserve(request.getProductId(), request.getQuantity());
        if (result == 0) {
            throw new InsufficientStockException("Failed to reserve - insufficient stock");
        }

        inventory = inventoryRepository.findByProductId(request.getProductId()).get();
        inventory.setStatus(InventoryStatus.RESERVED);
        inventoryRepository.save(inventory);
        
        log.info("Stock reserved successfully. Stock: {}, Reserved: {}", inventory.getStock(), inventory.getReserved());
        
        return mapToDTO(inventory);
    }

    @Override
    @Transactional
    public InventoryResponseDTO markAsSold(InventoryRequestDTO request) {
        log.info("Marking {} quantity as sold for product {}", request.getQuantity(), request.getProductId());
        
        validateRequest(request);

        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new InvalidProductException("Product not found: " + request.getProductId()));

        if (inventory.getReserved() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient reserved stock. Reserved: " + inventory.getReserved());
        }

        // Atomic operation
        int result = inventoryRepository.markAsSold(request.getProductId(), request.getQuantity());
        if (result == 0) {
            throw new InsufficientStockException("Failed to mark as sold - insufficient reserved stock");
        }

        inventory = inventoryRepository.findByProductId(request.getProductId()).get();
        inventory.setStatus(InventoryStatus.SOLD);
        inventoryRepository.save(inventory);
        
        log.info("Marked as sold successfully. Sold count: {}, Reserved: {}", inventory.getSoldCount(), inventory.getReserved());
        
        return mapToDTO(inventory);
    }

    @Override
    @Transactional
    public InventoryResponseDTO releaseReserved(InventoryRequestDTO request) {
        log.info("Releasing {} reserved stock for product {}", request.getQuantity(), request.getProductId());
        
        validateRequest(request);

        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new InvalidProductException("Product not found: " + request.getProductId()));

        if (inventory.getReserved() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient reserved stock. Reserved: " + inventory.getReserved());
        }

        // Atomic operation
        int result = inventoryRepository.releaseReserved(request.getProductId(), request.getQuantity());
        if (result == 0) {
            throw new InsufficientStockException("Failed to release - insufficient reserved stock");
        }

        inventory = inventoryRepository.findByProductId(request.getProductId()).get();
        inventory.setStatus(inventory.getAvailable() > 0 ? InventoryStatus.AVAILABLE : InventoryStatus.SOLD);
        inventoryRepository.save(inventory);
        
        log.info("Reserved stock released successfully. Stock: {}, Reserved: {}", inventory.getStock(), inventory.getReserved());
        
        return mapToDTO(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> getAll() {
        log.debug("Retrieving all inventory");
        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDTO getById(Long inventoryId) {
        log.debug("Retrieving inventory by id: {}", inventoryId);
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InvalidProductException("Inventory not found: " + inventoryId));
        
        return mapToDTO(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDTO getByProductId(Long productId) {
        log.debug("Retrieving inventory by product id: {}", productId);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InvalidProductException("Inventory not found for product: " + productId));
        
        return mapToDTO(inventory);
    }

    /**
     * Validate inventory request
     */
    private void validateRequest(InventoryRequestDTO request) {
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new IllegalArgumentException("Product ID must be valid");
        }
        
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new NegativeQuantityException("Quantity must be positive");
        }
    }

    /**
     * Convert Inventory entity to DTO with product details
     */
    private InventoryResponseDTO mapToDTO(Inventory inventory) {
        return InventoryResponseDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .productPrice(inventory.getProduct().getPrice())
                .stock(inventory.getStock())
                .reserved(inventory.getReserved())
                .available(inventory.getAvailable())
                .soldCount(inventory.getSoldCount())
                .status(inventory.getStatus().toString())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
