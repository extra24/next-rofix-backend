package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.dto.CodySearchRequest;
import com.rofix.fitspot.webservice.api.user.dto.CodySearchResult;
import com.rofix.fitspot.webservice.api.user.entity.Cody;
import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.repository.CodyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CodyService {

    @Autowired
    private CodyRepository codyRepository;


     //Cody를 검색
     //@param searchRequest 검색 조건
     //@return 검색된 코디 목록
    public List<CodySearchResult> searchCodies(CodySearchRequest searchRequest) {
        log.info("코디 검색 시작 - 검색텍스트: {}, 범위: {}, 카테고리: {}, 정렬: {}",
                searchRequest.getSearchText(), searchRequest.getSearchScope(),
                searchRequest.getCategory(), searchRequest.getSortBy());

        List<Object[]> rawResults = getSearchResults(searchRequest);
        List<CodySearchResult> results = convertToSearchResults(rawResults);

        //각 코디에 포함된 옷들 정보 추가
        results = addClothingInfo(results);

        //정렬 적용
        results = applySorting(results, searchRequest.getSortBy());

        log.info("검색 완료 - 결과 개수: {}", results.size());
        return results;
    }

    //검색 범위에 따라 적절한 쿼리를 실행
    private List<Object[]> getSearchResults(CodySearchRequest searchRequest) {
        String searchText = searchRequest.getSearchText();
        String category = searchRequest.getCategory();
        String searchScope = searchRequest.getSearchScope();

        switch (searchScope) {
            case "title":
                return codyRepository.searchByTitle(searchText, category);
            case "description":
                return codyRepository.searchByDescription(searchText, category);
            case "both":
            default:
                return codyRepository.searchByTitleAndDescription(searchText, category);
        }
    }

    //쿼리 결과를 CodySearchResult로 변환
    private List<CodySearchResult> convertToSearchResults(List<Object[]> rawResults) {
        List<CodySearchResult> results = new ArrayList<>();

        for (Object[] row : rawResults) {
            Cody cody = (Cody) row[0];
            Long likeCount = (Long) row[1];
            Long commentCount = (Long) row[2];

            CodySearchResult result = new CodySearchResult(cody, likeCount, commentCount);
            results.add(result);
        }

        return results;
    }

    //각 코디에 포함된 옷들의 정보를 추가
    private List<CodySearchResult> addClothingInfo(List<CodySearchResult> results) {
        for (CodySearchResult result : results) {
            List<Object> clothingObjects = codyRepository.findClothingsByCodyId(result.getCodyId());

            List<CodySearchResult.ClothingInCody> clothings = clothingObjects.stream()
                    .filter(obj -> obj instanceof Clothing)
                    .map(obj -> {
                        Clothing clothing = (Clothing) obj;
                        return new CodySearchResult.ClothingInCody(
                                clothing.getClothingId(),
                                clothing.getTitle(),
                                clothing.getCategory(),
                                clothing.getColor(),
                                clothing.getImageUrl(),
                                clothing.getBrand(),
                                clothing.getSeason()
                        );
                    })
                    .collect(Collectors.toList());

            result.setClothings(clothings);
        }

        return results;
    }

    //정렬 기준에 따라 결과를 정렬
    private List<CodySearchResult> applySorting(List<CodySearchResult> results, String sortBy) {
        switch (sortBy) {
            case "alphabetical":
                return results.stream()
                        .sorted(Comparator.comparing(CodySearchResult::getTitle))
                        .collect(Collectors.toList());
            case "likes":
                return results.stream()
                        .sorted(Comparator.comparing(CodySearchResult::getLikeCount).reversed())
                        .collect(Collectors.toList());
            case "latest":
            default:
                return results.stream()
                        .sorted(Comparator.comparing(CodySearchResult::getCreatedAt).reversed())
                        .collect(Collectors.toList());
        }
    }

    //코디에 포함된 옷들의 카테고리 목록을 조회
    public List<String> getAvailableCategories() {
        return codyRepository.findDistinctClothingCategories();
    }

    //특정 코디의 상세 정보를 조회
    public CodySearchResult getCodyDetail(Long codyId) {
        return codyRepository.findById(codyId)
                .map(cody -> {
                    //좋아요 수와 댓글 수 계산
                    long likeCount = cody.getLikes() != null ? cody.getLikes().size() : 0;
                    long commentCount = cody.getComments() != null ? cody.getComments().size() : 0;

                    CodySearchResult result = new CodySearchResult(cody, likeCount, commentCount);

                    //포함된 옷들 정보 추가
                    List<CodySearchResult> resultList = List.of(result);
                    resultList = addClothingInfo(resultList);

                    return resultList.get(0);
                })
                .orElse(null);
    }
}