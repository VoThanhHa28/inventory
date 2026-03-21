package com.project.inventory.entity;

import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@MappedSuperclass
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Tự tăng
    private Long id;

    @Column(name = "create_at", updatable = false) //Không cho phép sửa
    @CreationTimestamp //Tự động lấy giờ hiện tại khi insert
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    @CreationTimestamp //Tự động lấy giờ hiện tại khi update
    private LocalDateTime updatedAt;
}
