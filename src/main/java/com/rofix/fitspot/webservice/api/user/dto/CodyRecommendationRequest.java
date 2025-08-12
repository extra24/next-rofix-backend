package com.rofix.fitspot.webservice.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodyRecommendationRequest {
    private String personalColor;  // WARM, COOL 등
    private String weather;        // SUNNY, RAIN, COLD, HOT 등
    private Long userId;           // 사용자 ID
    private Boolean force;         // 강제 새 조합 생성 여부

    // 기본값 설정
    public Boolean getForce() {
        return force != null ? force : false;
    }
}