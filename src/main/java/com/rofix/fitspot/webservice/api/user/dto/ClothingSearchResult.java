package com.rofix.fitspot.webservice.api.user.dto;

import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClothingSearchResult {

    private Long clothingId;
    private String title;
    private String category;
    private String color;
    private String imageUrl;
    private String brand;
    private String season;
    private String description;
    private Long likeCount;     // 좋아요 개수
    private Boolean isLiked;    // 현재 사용자의 좋아요 여부 (추후 구현)

    // Clothing 엔티티로부터 생성하는 생성자
    public ClothingSearchResult(Clothing clothing, Long likeCount) {
        this.clothingId = clothing.getClothingId();
        this.title = clothing.getTitle();
        this.category = clothing.getCategory();
        this.color = clothing.getColor();
        this.imageUrl = clothing.getImageUrl();
        this.brand = clothing.getBrand();
        this.season = clothing.getSeason();
        this.description = clothing.getDescription();
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.isLiked = false; // 추후 사용자 정보와 연동하여 설정
    }
}