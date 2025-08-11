package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.dto.CodySearchRequest;
import com.rofix.fitspot.webservice.api.user.dto.CodySearchResult;
import com.rofix.fitspot.webservice.api.user.service.CodyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cody")
@Slf4j
public class CodyController {

    @Autowired
    private CodyService codyService;

    /**
     * 코디 검색 (GET 방식)
     * GET /api/cody/search?searchText=텍스트&searchScope=title&category=상의&sortBy=latest
     */
    @GetMapping("/search")
    public ResponseEntity<List<CodySearchResult>> searchCodies(
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "title") String searchScope,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "latest") String sortBy
    ) {
        CodySearchRequest searchRequest = new CodySearchRequest(
                searchText, searchScope, category, sortBy
        );

        List<CodySearchResult> results = codyService.searchCodies(searchRequest);

        if (results.isEmpty()) {
            log.info("검색 조건에 맞는 코디가 없습니다.");
        }

        return ResponseEntity.ok(results);
    }

    /**
     * POST 방식의 코디 검색 (복잡한 검색 조건을 위해)
     */
    @PostMapping("/search")
    public ResponseEntity<List<CodySearchResult>> searchCodiesPost(
            @RequestBody CodySearchRequest searchRequest
    ) {
        List<CodySearchResult> results = codyService.searchCodies(searchRequest);

        if (results.isEmpty()) {
            log.info("검색 조건에 맞는 코디가 없습니다.");
        }

        return ResponseEntity.ok(results);
    }

    /**
     * 사용 가능한 카테고리 목록 조회 (코디에 포함된 옷들의 카테고리)
     */
    @GetMapping("/search/categories")
    public ResponseEntity<List<String>> getAvailableCategories() {
        List<String> categories = codyService.getAvailableCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 코디 상세 정보 조회
     */
    @GetMapping("/{codyId}")
    public ResponseEntity<CodySearchResult> getCodyDetail(@PathVariable Long codyId) {
        CodySearchResult detail = codyService.getCodyDetail(codyId);

        if (detail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(detail);
    }
    
}