package com.rofix.fitspot.webservice.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodyRecommendationResponse {
    private List<RecommendedCody> codys;
    private boolean fallback;  // TOP 또는 BOTTOM 후보가 없는 경우 true

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedCody {
        private Long codyId;
        private String weather;
        private String title;
        private String description;
        private LocalDateTime createdAt;
        private Long likeCount;
        private List<CodyItem> items;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CodyItem {
            private Long clothingId;
            private String category;
            private String title;
            private String color;
            private String imageUrl;
            private String brand;
        }
    }
}