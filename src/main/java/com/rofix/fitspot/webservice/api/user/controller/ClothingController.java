package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.dto.ClothingDTO;
import com.rofix.fitspot.webservice.api.user.dto.ClothingRequestDTO;
import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.service.ClothingService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/clothes")
@Slf4j
@AllArgsConstructor
public class ClothingController {

    private final ClothingService clothingService;

    // 전체 옷 조회
    @GetMapping
    public List<ClothingDTO> getAllClothes() {return clothingService.getAllClothes();}

    // userId 별 옷 조회
    @GetMapping("/user")
    public ResponseEntity<List<ClothingDTO>> getMyClothes(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // ===== 로컬 개발용 가짜 세션 코드 (커밋 전 삭제 필요) =====
        if (currentUser == null) {
            String activeProfile = System.getProperty("spring.profiles.active", "local");
            if ("local".equals(activeProfile) || "dev".equals(activeProfile)) {
                log.info("로컬 개발 환경에서 가짜 사용자 세션 생성 (옷장 조회)");
                currentUser = createFakeUser();
                session.setAttribute("user", currentUser);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        // ===== 가짜 세션 코드 끝 =====

        // 서비스 메서드를 세션 기반으로 호출하도록 변경
        List<ClothingDTO> clothesList = clothingService.getClothesByUserID(currentUser.getUserId());
        return ResponseEntity.ok(clothesList);
    }

    // 옷 생성
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createClothing(
            @RequestPart("clothing") ClothingRequestDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpSession session
    ) {
        User currentUser = (User) session.getAttribute("user");

        // ===== 로컬 개발용 가짜 세션 코드 (커밋 전 삭제 필요) =====
        if (currentUser == null) {
            String activeProfile = System.getProperty("spring.profiles.active", "local");
            if ("local".equals(activeProfile) || "dev".equals(activeProfile)) {
                log.info("로컬 개발 환경에서 가짜 사용자 세션 생성 (옷 업로드)");
                currentUser = createFakeUser();
                session.setAttribute("user", currentUser);
            } else {
                log.warn("옷 업로드 시 인증되지 않은 사용자 접근");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }
        }
        // ===== 가짜 세션 코드 끝 =====

        try {
            ClothingDTO saved = clothingService.createClothing(dto, file, currentUser.getUserId());
            log.info("옷 업로드 성공: userId={}, clothingTitle={}", currentUser.getUserId(), dto.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("옷 업로드 실패: userId={}, clothingTitle={}, error={}", currentUser.getUserId(), dto.getTitle(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("옷 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 옷 삭제
    @DeleteMapping("/{clothingId}")
    public ResponseEntity<Void> deleteClothing(@PathVariable Long clothingId) {
        log.info("DELETED ID 해결: "+String.valueOf(clothingId));
        clothingService.deleteClothing(clothingId);
        return ResponseEntity.noContent().build();
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
