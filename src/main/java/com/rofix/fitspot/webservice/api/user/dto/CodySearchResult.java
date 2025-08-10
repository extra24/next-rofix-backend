package com.rofix.fitspot.webservice.api.user.dto;

import com.rofix.fitspot.webservice.api.user.entity.Cody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodySearchResult {

    private Long codyId;
    private String title;
    private String description;
    private String weather;
    private LocalDateTime createdAt;
    private Long likeCount;         // 좋아요 개수
    private Long commentCount;      // 댓글 개수
    private List<ClothingInCody> clothings;  // 포함된 옷들

    // 포함된 옷 정보를 위한 내부 클래스
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClothingInCody {
        private Long clothingId;
        private String title;
        private String category;
        private String color;
        private String imageUrl;
        private String brand;
        private String season;
    }

    // Cody 엔티티로부터 생성하는 생성자 (clothings는 별도 설정)
    public CodySearchResult(Cody cody, Long likeCount, Long commentCount) {
        this.codyId = cody.getCodyId();
        this.title = cody.getTitle();
        this.description = cody.getDescription();
        this.weather = cody.getWeather();
        this.createdAt = cody.getCreatedAt();
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.commentCount = commentCount != null ? commentCount : 0L;
    }
}