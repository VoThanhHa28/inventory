package com.project.inventory.controller;

import com.project.inventory.dto.InventoryRequestDTO;
import com.project.inventory.dto.InventoryResponseDTO;
import com.project.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * InventoryController - REST API for inventory management
 * 
 * Endpoints:
 * - POST /api/inventory/add-stock (ADMIN)
 * - POST /api/inventory/remove-stock (ADMIN)
 * - POST /api/inventory/reserve (ADMIN)
 * - POST /api/inventory/sold (ADMIN)
 * - GET /api/inventory (ALL)
 * - GET /api/inventory/{id} (ALL)
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/add-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add stock to inventory", description = "Increase available stock for a product (Admin only)")
    public ResponseEntity<?> addStock(@Valid @RequestBody InventoryRequestDTO request) {
        try {
            return ResponseEntity.ok(inventoryService.addStock(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/remove-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove stock from inventory", description = "Decrease available stock (Admin only)")
    public ResponseEntity<?> removeStock(@Valid @RequestBody InventoryRequestDTO request) {
        try {
            return ResponseEntity.ok(inventoryService.removeStock(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reserve stock", description = "Reserve stock for an order (Admin only)")
    public ResponseEntity<?> reserve(@Valid @RequestBody InventoryRequestDTO request) {
        try {
            return ResponseEntity.ok(inventoryService.reserve(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/sold")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark as sold", description = "Mark reserved stock as sold (Admin only)")
    public ResponseEntity<?> markAsSold(@Valid @RequestBody InventoryRequestDTO request) {
        try {
            return ResponseEntity.ok(inventoryService.markAsSold(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/release")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Release reserved stock", description = "Release reserved stock back to available (Admin only)")
    public ResponseEntity<?> releaseReserved(@Valid @RequestBody InventoryRequestDTO request) {
        try {
            return ResponseEntity.ok(inventoryService.releaseReserved(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "List all inventory", description = "Get all inventory with product details")
    public ResponseEntity<List<InventoryResponseDTO>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory by ID", description = "Get inventory details with product info")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventoryService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product ID", description = "Get inventory details by product ID")
    public ResponseEntity<?> getByProductId(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(inventoryService.getByProductId(productId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
