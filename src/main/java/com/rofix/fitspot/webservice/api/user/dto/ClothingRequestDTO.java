package com.rofix.fitspot.webservice.api.user.dto;

import lombok.Data;

@Data
public class ClothingRequestDTO {
    private Long userId;
    private String title;
    private String category;
    private String color;
    private String brand;
    private String weather;
    private String description;
}
