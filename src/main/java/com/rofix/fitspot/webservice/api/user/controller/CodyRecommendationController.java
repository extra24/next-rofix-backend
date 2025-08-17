package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.dto.CodyRecommendationRequest;
import com.rofix.fitspot.webservice.api.user.dto.CodyRecommendationResponse;
import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.service.CodyRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/cody")
@Slf4j
public class CodyRecommendationController {

    @Autowired
    private CodyRecommendationService codyRecommendationService;

    /**
     * 날씨 기반 코디 추천 (GET 방식)
     * GET /api/cody/weather/{weather}?personalColor=WARM&force=false
     */
    @GetMapping("/weather/{weather}")
    public ResponseEntity<CodyRecommendationResponse> recommendCodyByWeather(
            @PathVariable String weather,
            @RequestParam(required = false) String personalColor,
            @RequestParam(defaultValue = "false") Boolean force,
            HttpSession session
    ) {
        User currentUser = (User) session.getAttribute("user");

        // ===== 로컬 개발용 가짜 세션 코드 (커밋 전 삭제 필요) =====
        if (currentUser == null) {
            // 프로파일이 local이거나 개발 환경인 경우 가짜 사용자 생성
            String activeProfile = System.getProperty("spring.profiles.active", "local");
            if ("local".equals(activeProfile) || "dev".equals(activeProfile)) {
                log.info("로컬 개발 환경에서 가짜 사용자 세션 생성");
                currentUser = createFakeUser();
                session.setAttribute("user", currentUser);
            } else {
                log.warn("코디 추천 시 인증되지 않은 사용자 접근");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        // ===== 가짜 세션 코드 끝 =====

        log.info("날씨 기반 코디 추천 요청 - 날씨: {}, 사용자: {}, 퍼스널컬러: {}, 강제생성: {}",
                weather, currentUser.getUserId(), personalColor, force);

        CodyRecommendationRequest request = new CodyRecommendationRequest(
                personalColor, weather, currentUser.getUserId(), force
        );

        CodyRecommendationResponse response = codyRecommendationService.recommendCody(request);

        if (response.isFallback()) {
            log.warn("충분한 의상이 없어 추천 실패 - 사용자: {}", currentUser.getUserId());
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

    // ===== 로컬 개발용 가짜 사용자 생성 메서드 (커밋 전 삭제 필요) =====
    /**
     * 로컬 개발용 가짜 사용자 생성
     */
    private User createFakeUser() {
        User fakeUser = new User();
        fakeUser.setUserId(1L);
        fakeUser.setNickname("테스트유저");
        fakeUser.setEmail("test@example.com");
        return fakeUser;
    }
    // ===== 가짜 사용자 생성 메서드 끝 =====
}