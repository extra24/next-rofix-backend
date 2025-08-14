package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.dto.CodySearchRequest;
import com.rofix.fitspot.webservice.api.user.dto.CodySearchResult;
import com.rofix.fitspot.webservice.api.user.service.CodySearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cody")
@Slf4j
public class CodySearchController {

    @Autowired
    private CodySearchService codySearchService;

    /**
     * 코디 검색 (GET 방식)
     * GET /api/cody/search?searchText=텍스트&searchScope=title&category=winter&sortBy=latest
     */
    @GetMapping("/search")
    public ResponseEntity<List<CodySearchResult>> searchCodies(
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "title") String searchScope,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "latest") String sortBy
    ) {
        // 디버깅 로그 추가
        log.info("=== 코디 검색 API 호출 ===");
        log.info("searchText: '{}'", searchText);
        log.info("searchScope: '{}'", searchScope);
        log.info("category(실제로는 weather): '{}'", category);
        log.info("sortBy: '{}'", sortBy);

        CodySearchRequest searchRequest = new CodySearchRequest(
                searchText, searchScope, category, sortBy
        );

        List<CodySearchResult> results = codySearchService.searchCodies(searchRequest);

        // 결과 로그
        log.info("검색 결과 개수: {}", results.size());
        if (results.isEmpty()) {
            log.info("검색 조건에 맞는 코디가 없습니다.");
        } else {
            // 첫 번째 결과의 정보 로그
            CodySearchResult firstResult = results.get(0);
            log.info("첫 번째 결과: ID={}, 제목='{}', 날씨='{}'",
                    firstResult.getCodyId(), firstResult.getTitle(), firstResult.getWeather());
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
        List<CodySearchResult> results = codySearchService.searchCodies(searchRequest);

        if (results.isEmpty()) {
            log.info("검색 조건에 맞는 코디가 없습니다.");
        }

        return ResponseEntity.ok(results);
    }

    /**
     * 사용 가능한 날씨 카테고리 목록 조회
     */
    @GetMapping("/search/categories")
    public ResponseEntity<List<String>> getAvailableCategories() {
        List<String> categories = codySearchService.getAvailableCategories();
        log.info("사용 가능한 날씨 카테고리: {}", categories);
        return ResponseEntity.ok(categories);
    }

    /**
     * 코디 상세 정보 조회
     */
    @GetMapping("/{codyId}")
    public ResponseEntity<CodySearchResult> getCodyDetail(@PathVariable Long codyId) {
        CodySearchResult detail = codySearchService.getCodyDetail(codyId);

        if (detail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(detail);
    }
}