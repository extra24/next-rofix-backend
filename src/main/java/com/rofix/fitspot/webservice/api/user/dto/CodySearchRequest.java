package com.rofix.fitspot.webservice.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodySearchRequest {

    private String searchText;      // 검색 텍스트
    private String searchScope;     // 검색 범위 ("title", "description", "both")
    private String category;        // 카테고리 필터 (포함된 옷의 카테고리)
    private String sortBy;          // 정렬 기준 ("latest", "alphabetical", "likes")

    // 기본값 설정
    public String getSearchScope() {
        return searchScope != null ? searchScope : "title";
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "latest";
    }
}