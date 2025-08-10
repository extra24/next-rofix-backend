package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.dto.ClothingSearchRequest;
import com.rofix.fitspot.webservice.api.user.dto.ClothingSearchResult;
import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.repository.ClothingRepository;
import com.rofix.fitspot.webservice.aws.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClothingService {

    @Autowired
    private ClothingRepository clothingRepository;

    @Autowired
    private S3Service s3Service;

    // 옷 전체 조회
    public List<Clothing> getAllClothes() {return clothingRepository.findAll();}

    // userId 별 옷 조회
    public List<Clothing> getClothesByUserID(Long userId) {
        return clothingRepository.findByUserUserId(userId);
    }

    // 옷 생성
    public Clothing createClothing(Clothing clothing, MultipartFile file) throws IOException {
        String key = null;
        try {
            if (file != null && !file.isEmpty()) {
                key = s3Service.uploadFile(file, clothing.getUser().getUserId());
                clothing.setImageKey(key);
                clothing.setImageUrl(s3Service.getUrlForKey(key));
            }
            return clothingRepository.save(clothing);
        } catch (Exception e) {
            if (key != null) {
                try {
                    s3Service.deleteFile(key);
                } catch (Exception ex) {
                    log.warn("S3 rollback failed for key {}", key, ex);
                }
            }
            throw e;
        }
    }

    // 옷 삭제
    public void deleteClothing(Long clothingId) {
        clothingRepository.findById(clothingId).ifPresent( clothing -> {
            if (clothing.getImageKey() != null) {
                try {
                    s3Service.deleteFile(clothing.getImageKey());
                } catch (Exception e) {
                    log.warn("S3 delete failed for key {}", clothing.getImageKey(), e);
                }
            }
            clothingRepository.deleteById(clothingId);
        });
    }


    // Cody 테이블을 기반으로 의상을 검색
    // @param searchRequest 검색 조건
    // @return 검색된 의상 목록
    public List<ClothingSearchResult> searchClothings(ClothingSearchRequest searchRequest) {
        log.info("옷 검색 시작 - 검색텍스트: {}, 범위: {}, 카테고리: {}, 정렬: {}",
                searchRequest.getSearchText(), searchRequest.getSearchScope(),
                searchRequest.getCategory(), searchRequest.getSortBy());

        List<Object[]> rawResults = getSearchResults(searchRequest);
        List<ClothingSearchResult> results = convertToSearchResults(rawResults);

        // 정렬 적용
        results = applySorting(results, searchRequest.getSortBy());

        log.info("검색 완료 - 결과 개수: {}", results.size());
        return results;
    }


     //검색 범위에 따라 적절한 쿼리를 실행.

    private List<Object[]> getSearchResults(ClothingSearchRequest searchRequest) {
        String searchText = searchRequest.getSearchText();
        String category = searchRequest.getCategory();
        String searchScope = searchRequest.getSearchScope();

        switch (searchScope) {
            case "title":
                return clothingRepository.searchClothingByCodyTitle(searchText, category);
            case "description":
                return clothingRepository.searchClothingByCodyDescription(searchText, category);
            case "both":
            default:
                return clothingRepository.searchClothingByCodyTitleAndDescription(searchText, category);
        }
    }


     //쿼리 결과를 ClothingSearchResult로 변환.
    private List<ClothingSearchResult> convertToSearchResults(List<Object[]> rawResults) {
        List<ClothingSearchResult> results = new ArrayList<>();

        for (Object[] row : rawResults) {
            Clothing clothing = (Clothing) row[0];
            Long likeCount = (Long) row[1];

            ClothingSearchResult result = new ClothingSearchResult(clothing, likeCount);
            results.add(result);
        }

        return results;
    }


    //정렬 기준에 따라 결과를 정렬.
    private List<ClothingSearchResult> applySorting(List<ClothingSearchResult> results, String sortBy) {
        switch (sortBy) {
            case "alphabetical":
                return results.stream()
                        .sorted(Comparator.comparing(ClothingSearchResult::getTitle))
                        .collect(Collectors.toList());
            case "likes":
                return results.stream()
                        .sorted(Comparator.comparing(ClothingSearchResult::getLikeCount).reversed())
                        .collect(Collectors.toList());
            case "latest":
            default:
                // 최신순은 이미 데이터베이스에서 clothingId 순으로 정렬되어 나오므로 역순으로 정렬
                return results.stream()
                        .sorted(Comparator.comparing(ClothingSearchResult::getClothingId).reversed())
                        .collect(Collectors.toList());
        }
    }

    //Cody에 포함된 의상들의 카테고리 목록을 조회.
    public List<String> getAvailableCategories() {
        return clothingRepository.findDistinctCategoriesFromCody();
    }

    //특정 의상의 상세 정보를 조회.
    public ClothingSearchResult getClothingDetail(Long clothingId) {
        return clothingRepository.findById(clothingId)
                .map(clothing -> {
                    // 해당 의상의 좋아요 수 계산
                    List<Object[]> likeCountResult = clothingRepository
                            .searchClothingByCodyTitleAndDescription(null, null)
                            .stream()
                            .filter(row -> ((Clothing) row[0]).getClothingId().equals(clothingId))
                            .collect(Collectors.toList());

                    Long likeCount = likeCountResult.isEmpty() ? 0L : (Long) likeCountResult.get(0)[1];
                    return new ClothingSearchResult(clothing, likeCount);
                })
                .orElse(null);
    }

}
