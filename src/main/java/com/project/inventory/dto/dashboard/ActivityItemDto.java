package com.project.inventory.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.inventory.entity.ProductAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Activity Item DTO
 * Represents a single activity event in the dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityItemDto {
    
    /**
     * Product ID
     */
    private Long productId;
    
    /**
     * Product name at time of activity
     */
    private String productName;
    
    /**
     * Action performed (CREATE, UPDATE, DELETE)
     */
    private ProductAction action;
    
    /**
     * User who made the change
     */
    private String changedBy;
    
    /**
     * Timestamp of the activity
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changedAt;
    
    /**
     * Human-readable action label
     */
    public String getActionLabel() {
        if (action == null) return "UNKNOWN";
        switch (action) {
            case CREATE:
                return "Product Created";
            case UPDATE:
                return "Product Updated";
            case DELETE:
                return "Product Deleted";
            default:
                return action.toString();
        }
    }
    
    /**
     * Get action emoji for UI
     */
    public String getActionEmoji() {
        if (action == null) return "❓";
        switch (action) {
            case CREATE:
                return "✨";
            case UPDATE:
                return "✏️";
            case DELETE:
                return "🗑️";
            default:
                return "•";
        }
    }
}
