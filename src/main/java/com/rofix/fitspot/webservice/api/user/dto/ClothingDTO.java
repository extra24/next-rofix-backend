package com.rofix.fitspot.webservice.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClothingDTO {
    private Long clothingId;
    private Long userId;   // User 객체 대신 userId만
    private String title;
    private String category;
    private String color;
    private String imageUrl;
    private String imageKey;
    private String brand;
    private String weather;
    private String description;
    private LocalDateTime createdAt;
}
