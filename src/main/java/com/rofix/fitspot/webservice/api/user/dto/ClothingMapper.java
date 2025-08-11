package com.rofix.fitspot.webservice.api.user.dto;

import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.entity.User;

public class ClothingMapper {
    public static ClothingDTO toDTO(Clothing clothing) {
        return new ClothingDTO(
            clothing.getClothingId(),
            clothing.getUser() != null ? clothing.getUser().getUserId() : null,
            clothing.getTitle(),
            clothing.getCategory(),
            clothing.getColor(),
            clothing.getImageUrl(),
            clothing.getImageKey(),
            clothing.getBrand(),
            clothing.getWeather(),
            clothing.getDescription(),
            clothing.getCreatedAt()
        );
    }

    public static Clothing toEntity(ClothingRequestDTO dto) {
        Clothing clothing = new Clothing();
        if (dto.getUserId() != null) {
            User user = new User();
            user.setUserId(dto.getUserId());
            clothing.setUser(user);
        }
        clothing.setTitle(dto.getTitle());
        clothing.setCategory(dto.getCategory());
        clothing.setColor(dto.getColor());
        clothing.setBrand(dto.getBrand());
        clothing.setWeather(dto.getWeather());
        clothing.setDescription(dto.getDescription());
        return clothing;
    }
}
