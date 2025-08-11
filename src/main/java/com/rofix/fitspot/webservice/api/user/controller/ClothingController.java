package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.dto.ClothingDTO;
import com.rofix.fitspot.webservice.api.user.dto.ClothingRequestDTO;
import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.service.ClothingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ClothingController {

    @Autowired
    private ClothingService clothingService;

    // 전체 옷 조회
    @GetMapping
    public List<ClothingDTO> getAllClothes() {return clothingService.getAllClothes();}

    // userId 별 옷 조회
    @GetMapping("/user/{userId}")
    public List<ClothingDTO> getAllClothes (@PathVariable Long userId) {
        return clothingService.getClothesByUserID(userId);
    }

    // 옷 생성
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothingDTO> createClothing(
            @RequestPart("clothing") ClothingRequestDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        ClothingDTO saved = clothingService.createClothing(dto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 옷 삭제
    @DeleteMapping("/{clothingId}")
    public ResponseEntity<Void> deleteClothing(@PathVariable Long clothingId) {
        clothingService.deleteClothing(clothingId);
        return ResponseEntity.noContent().build();
    }
}
