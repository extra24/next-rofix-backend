package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.dto.CodyRecommendationRequest;
import com.rofix.fitspot.webservice.api.user.dto.CodyRecommendationResponse;
import com.rofix.fitspot.webservice.api.user.service.CodyRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cody")
@Slf4j
public class CodyRecommendationController {

    @Autowired
    private CodyRecommendationService codyRecommendationService;

    /**
     * 날씨 기반 코디 추천 (GET 방식)
     * GET /api/cody/weather/{weather}?userId=1&personalColor=WARM&force=false
     */
    @GetMapping("/weather/{weather}")
    public ResponseEntity<CodyRecommendationResponse> recommendCodyByWeather(
            @PathVariable String weather,
            @RequestParam Long userId,
            @RequestParam(required = false) String personalColor,
            @RequestParam(defaultValue = "false") Boolean force
    ) {
        log.info("날씨 기반 코디 추천 요청 - 날씨: {}, 사용자: {}, 퍼스널컬러: {}, 강제생성: {}",
                weather, userId, personalColor, force);

        CodyRecommendationRequest request = new CodyRecommendationRequest(
                personalColor, weather, userId, force
        );

        CodyRecommendationResponse response = codyRecommendationService.recommendCody(request);

        if (response.isFallback()) {
            log.warn("충분한 의상이 없어 추천 실패 - 사용자: {}", userId);
            return ResponseEntity.ok(response); // 200 OK지만 fallback=true
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 코디 추천 (POST 방식) - 복잡한 요청을 위해
     * POST /api/cody/recommend
     */
    @PostMapping("/recommend")
    public ResponseEntity<CodyRecommendationResponse> recommendCody(
            @RequestBody CodyRecommendationRequest request
    ) {
        log.info("코디 추천 요청 - 요청 정보: {}", request);

        CodyRecommendationResponse response = codyRecommendationService.recommendCody(request);

        if (response.isFallback()) {
            log.warn("충분한 의상이 없어 추천 실패 - 사용자: {}", request.getUserId());
        }

        return ResponseEntity.ok(response);
    }
}