package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.dto.ClothingSearchRequest;
import com.rofix.fitspot.webservice.api.user.dto.ClothingSearchResult;
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
    public List<Clothing> getAllClothes() {return clothingService.getAllClothes();}

    // userId 별 옷 조회
    @GetMapping("/user/{userId}")
    public List<Clothing> getAllClothes (@PathVariable Long userId) {
        return clothingService.getClothesByUserID(userId);
    }

    // 옷 생성
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Clothing> createClothing(
            @RequestPart("clothing") Clothing clothing,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Clothing saved = clothingService.createClothing(clothing, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 옷 삭제
    @DeleteMapping("/{clothingId}")
    public ResponseEntity<Void> deleteClothing(@PathVariable Long clothingId) {
        clothingService.deleteClothing(clothingId);
        return ResponseEntity.noContent().build();
    }

     //의상 검색
     //GET /api/clothes/search?searchText=텍스트&searchScope=title&category=상의&sortBy=latest
    @GetMapping("/search")
    public ResponseEntity<List<ClothingSearchResult>> searchClothings(
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "title") String searchScope,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "latest") String sortBy
    ) {
        ClothingSearchRequest searchRequest = new ClothingSearchRequest(
                searchText, searchScope, category, sortBy
        );

        List<ClothingSearchResult> results = clothingService.searchClothings(searchRequest);

        if (results.isEmpty()) {
            log.info("검색 조건에 맞는 의상이 없습니다.");
        }

        return ResponseEntity.ok(results);
    }


    //POST 방식의 의상 검색 (복잡한 검색 조건을 위해)
    @PostMapping("/search")
    public ResponseEntity<List<ClothingSearchResult>> searchClothingsPost(
            @RequestBody ClothingSearchRequest searchRequest
    ) {
        List<ClothingSearchResult> results = clothingService.searchClothings(searchRequest);

        if (results.isEmpty()) {
            log.info("검색 조건에 맞는 의상이 없습니다.");
        }

        return ResponseEntity.ok(results);
    }

    //사용 가능한 카테고리 목록 조회
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAvailableCategories() {
        List<String> categories = clothingService.getAvailableCategories();
        return ResponseEntity.ok(categories);
    }


    //의상 상세 정보 조회
    @GetMapping("/{clothingId}/detail")
    public ResponseEntity<ClothingSearchResult> getClothingDetail(@PathVariable Long clothingId) {
        ClothingSearchResult detail = clothingService.getClothingDetail(clothingId);

        if (detail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(detail);
    }

}
