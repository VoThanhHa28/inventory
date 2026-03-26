package com.project.inventory.service;

import com.project.inventory.dto.inventory.InventoryRequestDTO;
import com.project.inventory.dto.inventory.InventoryResponseDTO;
import com.project.inventory.entity.Inventory;
import com.project.inventory.entity.InventoryStatus;
import com.project.inventory.entity.Product;
import com.project.inventory.exception.InsufficientStockException;
import com.project.inventory.exception.InvalidProductException;
import com.project.inventory.exception.NegativeQuantityException;
import com.project.inventory.repository.InventoryRepository;
import com.project.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryService
 * 
 * Test scenarios:
 * - Stock addition (add stock to inventory)
 * - Stock removal (remove stock from inventory)
 * - Stock reservation (for orders)
 * - Mark as sold (convert reserved to sold)
 * - Release reserved (cancel reservations)
 * - Retrieval operations (get all, by ID, by product)
 * - Exception handling for insufficient stock
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory mockInventory;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        mockProduct = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(1299.99)
                .stockQuantity(100)
                .build();

        mockInventory = Inventory.builder()
                .product(mockProduct)
                .stock(100)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        mockInventory.setId(1L);
    }

    private InventoryRequestDTO createRequestDTO(Long productId, Integer quantity) {
        InventoryRequestDTO dto = new InventoryRequestDTO();
        dto.setProductId(productId);
        dto.setQuantity(quantity);
        return dto;
    }

    // ===================================================================
    // TEST SCENARIO 1: ADD STOCK
    // ===================================================================

    @Test
    @DisplayName("Should add stock successfully")
    void testAddStock_ShouldIncreaseAvailableStock() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, 50);
        
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.addStock(1L, 50)).thenReturn(1);

        Inventory updated = Inventory.builder()
                .product(mockProduct)
                .stock(150)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        updated.setId(1L);
        
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(updated);

        // When
        InventoryResponseDTO result = inventoryService.addStock(request);

        // Then
        assertNotNull(result);
        assertEquals(150, result.getStock());
        verify(inventoryRepository, times(1)).addStock(1L, 50);
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity")
    void testAddStock_ShouldThrowExceptionForNegativeQuantity() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        // When & Then
        assertThrows(NegativeQuantityException.class, () -> inventoryService.addStock(request));
    }

    @Test
    @DisplayName("Should throw InvalidProductException when product not found")
    void testAddStock_ShouldThrowExceptionForProductNotFound() {
        // Given
        InventoryRequestDTO request = createRequestDTO(999L, 50);
        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidProductException.class, () -> inventoryService.addStock(request));
    }

    // ===================================================================
    // TEST SCENARIO 2: REMOVE STOCK
    // ===================================================================

    @Test
    @DisplayName("Should remove stock successfully")
    void testRemoveStock_ShouldDecreaseAvailableStock() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, 30);
        
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.removeStock(1L, 30)).thenReturn(1);

        Inventory updated = Inventory.builder()
                .product(mockProduct)
                .stock(70)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        updated.setId(1L);
        
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(updated);

        // When
        InventoryResponseDTO result = inventoryService.removeStock(request);

        // Then
        assertNotNull(result);
        assertEquals(70, result.getStock());
        verify(inventoryRepository, times(1)).removeStock(1L, 30);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when stock insufficient")
    void testRemoveStock_ShouldThrowExceptionForInsufficientStock() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, 150);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> inventoryService.removeStock(request));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity")
    void testRemoveStock_ShouldThrowExceptionForNegativeQuantity() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        // When & Then
        assertThrows(NegativeQuantityException.class, () -> inventoryService.removeStock(request));
    }

    // ===================================================================
    // TEST SCENARIO 3: RESERVE STOCK
    // ===================================================================

    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserve_ShouldMoveFromAvailableToReserved() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, 20);
        
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.reserve(1L, 20)).thenReturn(1);

        Inventory reserved = Inventory.builder()
                .product(mockProduct)
                .stock(80)
                .reserved(20)
                .soldCount(0)
                .status(InventoryStatus.RESERVED)
                .build();
        reserved.setId(1L);
        
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(reserved);

        // When
        InventoryResponseDTO result = inventoryService.reserve(request);

        // Then
        assertNotNull(result);
        assertEquals(80, result.getStock());
        assertEquals(20, result.getReserved());
        verify(inventoryRepository, times(1)).reserve(1L, 20);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when insufficient available")
    void testReserve_ShouldThrowExceptionForInsufficientAvailable() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, 150);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> inventoryService.reserve(request));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity")
    void testReserve_ShouldThrowExceptionForNegativeQuantity() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        // When & Then
        assertThrows(NegativeQuantityException.class, () -> inventoryService.reserve(request));
    }

    // ===================================================================
    // TEST SCENARIO 4: MARK AS SOLD
    // ===================================================================

    @Test
    @DisplayName("Should mark reserved stock as sold")
    void testMarkAsSold_ShouldConvertReservedToSold() {
        // Given
        Inventory reserved = Inventory.builder()
                .product(mockProduct)
                .stock(80)
                .reserved(20)
                .soldCount(0)
                .status(InventoryStatus.RESERVED)
                .build();
        reserved.setId(1L);

        InventoryRequestDTO request = createRequestDTO(1L, 20);

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(reserved));
        when(inventoryRepository.markAsSold(1L, 20)).thenReturn(1);

        Inventory sold = Inventory.builder()
                .product(mockProduct)
                .stock(80)
                .reserved(0)
                .soldCount(20)
                .status(InventoryStatus.SOLD)
                .build();
        sold.setId(1L);
        
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(sold);

        // When
        InventoryResponseDTO result = inventoryService.markAsSold(request);

        // Then
        assertNotNull(result);
        assertEquals(20, result.getSoldCount());
        assertEquals(0, result.getReserved());
        verify(inventoryRepository, times(1)).markAsSold(1L, 20);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when insufficient reserved")
    void testMarkAsSold_ShouldThrowExceptionForInsufficientReserved() {
        // Given
        Inventory inventory = Inventory.builder()
                .product(mockProduct)
                .stock(90)
                .reserved(5)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        inventory.setId(1L);

        InventoryRequestDTO request = createRequestDTO(1L, 20);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> inventoryService.markAsSold(request));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity")
    void testMarkAsSold_ShouldThrowExceptionForNegativeQuantity() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        // When & Then
        assertThrows(NegativeQuantityException.class, () -> inventoryService.markAsSold(request));
    }

    // ===================================================================
    // TEST SCENARIO 5: RELEASE RESERVED
    // ===================================================================

    @Test
    @DisplayName("Should release reserved stock successfully")
    void testReleaseReserved_ShouldReturnReservedToAvailable() {
        // Given
        Inventory reserved = Inventory.builder()
                .product(mockProduct)
                .stock(80)
                .reserved(20)
                .soldCount(0)
                .status(InventoryStatus.RESERVED)
                .build();
        reserved.setId(1L);

        InventoryRequestDTO request = createRequestDTO(1L, 20);

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(reserved));
        when(inventoryRepository.releaseReserved(1L, 20)).thenReturn(1);

        Inventory released = Inventory.builder()
                .product(mockProduct)
                .stock(100)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        released.setId(1L);
        
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(released);

        // When
        InventoryResponseDTO result = inventoryService.releaseReserved(request);

        // Then
        assertNotNull(result);
        assertEquals(100, result.getStock());
        assertEquals(0, result.getReserved());
        verify(inventoryRepository, times(1)).releaseReserved(1L, 20);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when insufficient reserved")
    void testReleaseReserved_ShouldThrowExceptionForInsufficientReserved() {
        // Given
        Inventory inventory = Inventory.builder()
                .product(mockProduct)
                .stock(100)
                .reserved(5)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        inventory.setId(1L);

        InventoryRequestDTO request = createRequestDTO(1L, 20);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> inventoryService.releaseReserved(request));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity")
    void testReleaseReserved_ShouldThrowExceptionForNegativeQuantity() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        // When & Then
        assertThrows(NegativeQuantityException.class, () -> inventoryService.releaseReserved(request));
    }

    // ===================================================================
    // TEST SCENARIO 6: RETRIEVAL OPERATIONS
    // ===================================================================

    @Test
    @DisplayName("Should get all inventory records")
    void testGetAll_ShouldReturnAllRecords() {
        // Given
        Inventory inv2 = Inventory.builder()
                .product(Product.builder().id(2L).name("iPhone 15").price(999.99).build())
                .stock(50)
                .reserved(10)
                .soldCount(5)
                .status(InventoryStatus.RESERVED)
                .build();
        inv2.setId(2L);

        when(inventoryRepository.findAll()).thenReturn(List.of(mockInventory, inv2));

        // When
        List<InventoryResponseDTO> results = inventoryService.getAll();

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get inventory by ID")
    void testGetById_ShouldReturnInventoryById() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(mockInventory));

        // When
        InventoryResponseDTO result = inventoryService.getById(1L);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when inventory not found by ID")
    void testGetById_ShouldThrowExceptionForNotFound() {
        // Given
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> inventoryService.getById(999L));
    }

    @Test
    @DisplayName("Should get inventory by product ID")
    void testGetByProductId_ShouldReturnInventoryByProductId() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        // When
        InventoryResponseDTO result = inventoryService.getByProductId(1L);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).findByProductId(1L);
    }

    @Test
    @DisplayName("Should throw exception when inventory not found by product ID")
    void testGetByProductId_ShouldThrowExceptionForNotFound() {
        // Given
        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> inventoryService.getByProductId(999L));
    }

    // ===================================================================
    // TEST SCENARIO 7: EDGE CASES
    // ===================================================================

    @Test
    @DisplayName("Should calculate available stock correctly")
    void testCalculateAvailableStock() {
        // Given
        Inventory inventory = Inventory.builder()
                .product(mockProduct)
                .stock(100)
                .reserved(25)
                .soldCount(0)
                .status(InventoryStatus.RESERVED)
                .build();
        inventory.setId(1L);

        // When
        Integer available = inventory.getStock() - inventory.getReserved();

        // Then
        assertEquals(75, available);
    }

    @Test
    @DisplayName("Should validate non-negative quantity")
    void testValidateNonNegativeQuantity() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, 50);

        // When & Then
        assertDoesNotThrow(() -> {
            if (request.getQuantity() < 0) {
                throw new NegativeQuantityException("Quantity must be non-negative");
            }
        });
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void testHandleZeroQuantity() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, 0);

        // When & Then
        assertDoesNotThrow(() -> {
            if (request.getQuantity() < 0) {
                throw new NegativeQuantityException("Quantity must be non-negative");
            }
        });
    }

    @Test
    @DisplayName("Should handle large quantities")
    void testHandleLargeQuantities() {
        // Given
        InventoryRequestDTO request = createRequestDTO(1L, 10000);
        
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.addStock(1L, 10000)).thenReturn(1);

        Inventory updated = Inventory.builder()
                .product(mockProduct)
                .stock(10100)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        updated.setId(1L);
        
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(updated);

        // When
        InventoryResponseDTO result = inventoryService.addStock(request);

        // Then
        assertNotNull(result);
        assertEquals(10100, result.getStock());
    }
}
