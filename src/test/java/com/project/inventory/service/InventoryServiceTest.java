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
import com.project.inventory.repository.ProductRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory mockInventory;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        mockProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(100.0)
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

    @Test
    @DisplayName("Should add stock successfully")
    void testAddStock_ShouldIncreaseAvailableStock() {
        InventoryRequestDTO request = createRequestDTO(1L, 50);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.addStock(1L, 50)).thenReturn(1);

        InventoryResponseDTO result = inventoryService.addStock(request);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).addStock(1L, 50);
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity in addStock")
    void testAddStock_ShouldThrowExceptionForNegativeQuantity() {
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        assertThrows(NegativeQuantityException.class, () -> inventoryService.addStock(request));
        verify(inventoryRepository, never()).addStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should throw InvalidProductException when product not found in addStock")
    void testAddStock_ShouldThrowExceptionForProductNotFound() {
        InventoryRequestDTO request = createRequestDTO(999L, 50);
        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(InvalidProductException.class, () -> inventoryService.addStock(request));
        verify(inventoryRepository, never()).addStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should remove stock successfully")
    void testRemoveStock_ShouldDecreaseAvailableStock() {
        InventoryRequestDTO request = createRequestDTO(1L, 30);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.removeStock(1L, 30)).thenReturn(1);

        InventoryResponseDTO result = inventoryService.removeStock(request);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).removeStock(1L, 30);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when removing more than available")
    void testRemoveStock_ShouldThrowExceptionForInsufficientStock() {
        InventoryRequestDTO request = createRequestDTO(1L, 200);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.removeStock(request));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity in removeStock")
    void testRemoveStock_ShouldThrowExceptionForNegativeQuantity() {
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        assertThrows(NegativeQuantityException.class, () -> inventoryService.removeStock(request));
        verify(inventoryRepository, never()).removeStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserve_ShouldMoveFromAvailableToReserved() {
        InventoryRequestDTO request = createRequestDTO(1L, 40);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.reserve(1L, 40)).thenReturn(1);

        InventoryResponseDTO result = inventoryService.reserve(request);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).reserve(1L, 40);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when reserving more than available")
    void testReserve_ShouldThrowExceptionForInsufficientAvailable() {
        InventoryRequestDTO request = createRequestDTO(1L, 200);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.reserve(request));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity in reserve")
    void testReserve_ShouldThrowExceptionForNegativeQuantity() {
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        assertThrows(NegativeQuantityException.class, () -> inventoryService.reserve(request));
        verify(inventoryRepository, never()).reserve(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should mark reserved stock as sold")
    void testMarkAsSold_ShouldConvertReservedToSold() {
        InventoryRequestDTO request = createRequestDTO(1L, 25);
        mockInventory.setReserved(25);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.markAsSold(1L, 25)).thenReturn(1);

        InventoryResponseDTO result = inventoryService.markAsSold(request);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).markAsSold(1L, 25);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException if not enough reserved")
    void testMarkAsSold_ShouldThrowExceptionForInsufficientReserved() {
        InventoryRequestDTO request = createRequestDTO(1L, 200);
        mockInventory.setReserved(10);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.markAsSold(request));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity in markAsSold")
    void testMarkAsSold_ShouldThrowExceptionForNegativeQuantity() {
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        assertThrows(NegativeQuantityException.class, () -> inventoryService.markAsSold(request));
        verify(inventoryRepository, never()).markAsSold(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should release reserved stock back to available")
    void testReleaseReserved_ShouldReturnReservedToAvailable() {
        InventoryRequestDTO request = createRequestDTO(1L, 20);
        mockInventory.setReserved(20);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.releaseReserved(1L, 20)).thenReturn(1);

        InventoryResponseDTO result = inventoryService.releaseReserved(request);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).releaseReserved(1L, 20);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException if not enough reserved to release")
    void testReleaseReserved_ShouldThrowExceptionForInsufficientReserved() {
        InventoryRequestDTO request = createRequestDTO(1L, 50);
        mockInventory.setReserved(10);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.releaseReserved(request));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException for negative quantity in releaseReserved")
    void testReleaseReserved_ShouldThrowExceptionForNegativeQuantity() {
        InventoryRequestDTO request = createRequestDTO(1L, -10);

        assertThrows(NegativeQuantityException.class, () -> inventoryService.releaseReserved(request));
        verify(inventoryRepository, never()).releaseReserved(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should return all inventory records")
    void testGetAll_ShouldReturnAllRecords() {
        InventoryResponseDTO dto = InventoryResponseDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Product1")
                .stock(100)
                .build();
        when(inventoryRepository.findAll()).thenReturn(List.of(mockInventory));

        List<InventoryResponseDTO> result = inventoryService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return inventory by ID")
    void testGetById_ShouldReturnInventoryById() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(mockInventory));

        InventoryResponseDTO result = inventoryService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(inventoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when inventory not found by ID")
    void testGetById_ShouldThrowExceptionForNotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> inventoryService.getById(999L));
    }

    @Test
    @DisplayName("Should return inventory by product ID")
    void testGetByProductId_ShouldReturnInventoryByProductId() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        InventoryResponseDTO result = inventoryService.getByProductId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        verify(inventoryRepository, times(1)).findByProductId(1L);
    }

    @Test
    @DisplayName("Should throw exception when inventory not found by product ID")
    void testGetByProductId_ShouldThrowExceptionForNotFound() {
        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> inventoryService.getByProductId(999L));
    }
}
