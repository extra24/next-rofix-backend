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
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 서비스 메서드를 세션 기반으로 호출하도록 변경
        List<ClothingDTO> clothesList = clothingService.getClothesByUserID(currentUser.getUserId());
        return ResponseEntity.ok(clothesList);
    }

    // 옷 생성
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothingDTO> createClothing(
            @RequestPart("clothing") ClothingRequestDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpSession session
    ) throws IOException {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ClothingDTO saved = clothingService.createClothing(dto, file, currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 옷 삭제
    @DeleteMapping("/{clothingId}")
    public ResponseEntity<Void> deleteClothing(@PathVariable Long clothingId) {
        log.info("DELETED ID 해결: "+String.valueOf(clothingId));
        clothingService.deleteClothing(clothingId);
        return ResponseEntity.noContent().build();
    }

}
