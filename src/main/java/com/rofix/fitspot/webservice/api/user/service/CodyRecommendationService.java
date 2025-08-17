package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.dto.CodyRecommendationRequest;
import com.rofix.fitspot.webservice.api.user.dto.CodyRecommendationResponse;
import com.rofix.fitspot.webservice.api.user.entity.*;
import com.rofix.fitspot.webservice.api.user.repository.ClothingRepository;
import com.rofix.fitspot.webservice.api.user.repository.CodyClothesRepository;
import com.rofix.fitspot.webservice.api.user.repository.CodyRepository;
import com.rofix.fitspot.webservice.api.user.util.ColorMatchingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class CodyRecommendationService {

    private static final int MAX_CODY_COUNT = 3;
    private static final int MAX_COMBINATIONS = 6;

    @Autowired
    private ClothingRepository clothingRepository;

    @Autowired
    private CodyRepository codyRepository;

    @Autowired
    private CodyClothesRepository codyClothesRepository;

    /**
     * 코디 추천 메인 메서드
     */
    public CodyRecommendationResponse recommendCody(CodyRecommendationRequest request) {
        log.info("코디 추천 시작 - 사용자: {}, 날씨: {}, 퍼스널컬러: {}",
                request.getUserId(), request.getWeather(), request.getPersonalColor());

        try {
            // 기존 코디 확인
            if (!request.getForce()) {
                Optional<Cody> existingCody = findExistingCody(request);
                if (existingCody.isPresent()) {
                    log.info("기존 코디 재사용: {}", existingCody.get().getCodyId());
                    return createResponse(List.of(existingCody.get()));
                }
            }

            // 코디 생성
            List<Cody> newCodys = generateNewCodys(request);
            log.info("코디 추천 완료 - 생성된 코디 수: {}", newCodys.size());

            return createResponse(newCodys);

        } catch (Exception e) {
            log.error("코디 추천 중 오류 발생", e);
            return createFallbackResponse();
        }
    }

    /**
     * 새로운 코디 생성
     */
    private List<Cody> generateNewCodys(CodyRecommendationRequest request) {
        ClothingCandidates candidates = getClothingCandidates(request);

        if (candidates.isEmpty()) {
            log.warn("충분한 의상이 없어 추천 실패 - TOP: {}, BOTTOM: {}",
                    candidates.tops.size(), candidates.bottoms.size());
            return Collections.emptyList();
        }

        return createCodyCombinations(request, candidates);
    }

    /**
     * 기존 코디 조회
     */
    private Optional<Cody> findExistingCody(CodyRecommendationRequest request) {
        List<Cody> existingCodys = codyRepository.findByUserIdAndWeather(
                request.getUserId(), request.getWeather());
        return existingCodys.isEmpty() ? Optional.empty() : Optional.of(existingCodys.get(0));
    }

    /**
     * 후보 의상 조회
     */
    private ClothingCandidates getClothingCandidates(CodyRecommendationRequest request) {
        ClothingCandidates candidates = new ClothingCandidates();

        candidates.tops = getFilteredClothes(request, ClothingCategory.TOP);
        candidates.bottoms = getFilteredClothes(request, ClothingCategory.BOTTOM);
        candidates.shoes = getFilteredClothes(request, ClothingCategory.SHOES);

        if (shouldIncludeOuter(request.getWeather())) {
            candidates.outers = getFilteredClothes(request, ClothingCategory.OUTER);
        }

        logCandidatesSummary(candidates);
        return candidates;
    }

    /**
     * 카테고리별 필터링된 의상 조회
     */
    private List<Clothing> getFilteredClothes(CodyRecommendationRequest request, ClothingCategory category) {
        List<Clothing> clothes = clothingRepository.findByUserIdAndCategoryAndWeather(
                request.getUserId(), category.name(), request.getWeather());

        return clothes.stream()
                .filter(clothing -> isPersonalColorMatched(request.getPersonalColor(), clothing.getColor()))
                .toList();
    }

    /**
     * 퍼스널컬러 매칭 확인
     */
    private boolean isPersonalColorMatched(String personalColor, String clothingColor) {
        return ColorMatchingUtil.isColorMatched(personalColor, clothingColor);
    }

    /**
     * 아우터 포함 여부 결정
     */
    private boolean shouldIncludeOuter(String weather) {
        return WeatherType.RAIN.matches(weather) || WeatherType.COLD.matches(weather);
    }

    /**
     * 코디 조합 생성
     */
    private List<Cody> createCodyCombinations(CodyRecommendationRequest request, ClothingCandidates candidates) {
        List<Cody> generatedCodys = new ArrayList<>();

        for (Clothing top : candidates.tops) {
            for (Clothing bottom : candidates.bottoms) {
                if (generatedCodys.size() >= MAX_CODY_COUNT) break;

                for (Clothing shoe : getShoeOptions(candidates.shoes)) {
                    if (generatedCodys.size() >= MAX_CODY_COUNT) break;

                    Clothing outer = getOuterForCombination(candidates.outers, generatedCodys.size());
                    Cody cody = createOrFindCody(request, top, bottom, outer, shoe);

                    if (cody != null) {
                        generatedCodys.add(cody);
                    }
                }
            }
        }

        return generatedCodys;
    }

    /**
     * 신발 옵션 가져오기
     */
    private List<Clothing> getShoeOptions(List<Clothing> shoes) {
        return shoes.isEmpty() ? List.of((Clothing) null) : shoes;
    }

    /**
     * 조합에 맞는 아우터 선택
     */
    private Clothing getOuterForCombination(List<Clothing> outers, int index) {
        return (!outers.isEmpty() && index < outers.size()) ? outers.get(index) : null;
    }

    /**
     * 코디 생성 또는 기존 코디 찾기
     */
    private Cody createOrFindCody(CodyRecommendationRequest request, Clothing top, Clothing bottom,
                                  Clothing outer, Clothing shoe) {
        String hash = generateCodyHash(request, top, bottom, outer, shoe);

        Optional<Cody> existingCody = codyRepository.findByHash(hash);
        if (existingCody.isPresent()) {
            return existingCody.get();
        }

        return createNewCody(request, top, bottom, outer, shoe, hash);
    }

    /**
     * 새 코디 생성
     */
    private Cody createNewCody(CodyRecommendationRequest request, Clothing top, Clothing bottom,
                               Clothing outer, Clothing shoe, String hash) {
        Cody cody = buildCodyEntity(request, top, bottom, outer, shoe, hash);
        Cody savedCody = codyRepository.save(cody);

        saveCodyClothesRelations(savedCody, top, bottom, outer, shoe);

        log.info("새 코디 생성 완료: {} - {}", savedCody.getCodyId(), savedCody.getTitle());
        return savedCody;
    }

    /**
     * 코디 엔티티 구성
     */
    private Cody buildCodyEntity(CodyRecommendationRequest request, Clothing top, Clothing bottom,
                                 Clothing outer, Clothing shoe, String hash) {
        User user = new User();
        user.setUserId(request.getUserId());

        Cody cody = new Cody();
        cody.setUser(user);
        cody.setWeather(request.getWeather());
        cody.setTitle(generateCodyTitle(top, bottom, outer, shoe));
        cody.setDescription(generateCodyDescription(top, bottom, outer, shoe, request.getWeather()));
        cody.setCreatedAt(LocalDateTime.now());
        cody.setHash(hash);

        return cody;
    }

    /**
     * 코디-의상 관계 저장
     */
    private void saveCodyClothesRelations(Cody cody, Clothing top, Clothing bottom, Clothing outer, Clothing shoe) {
        saveCodyClothes(cody, top);
        saveCodyClothes(cody, bottom);
        saveCodyClothes(cody, outer);
        saveCodyClothes(cody, shoe);
    }

    /**
     * 코디-의상 관계 저장
     */
    private void saveCodyClothes(Cody cody, Clothing clothing) {
        if (clothing == null) return;

        CodyClothes codyClothes = new CodyClothes();
        codyClothes.setCody(cody);
        codyClothes.setClothing(clothing);
        codyClothesRepository.save(codyClothes);
    }

    /**
     * 코디 해시 생성
     */
    private String generateCodyHash(CodyRecommendationRequest request, Clothing top, Clothing bottom,
                                    Clothing outer, Clothing shoe) {
        List<Long> clothingIds = Arrays.asList(
                top.getClothingId(),
                bottom.getClothingId(),
                outer != null ? outer.getClothingId() : null,
                shoe != null ? shoe.getClothingId() : null
        );

        return generateHash(request.getUserId(), request.getWeather(),
                           request.getPersonalColor(), clothingIds);
    }

    /**
     * 해시 생성
     */
    private String generateHash(Long userId, String weather, String personalColor, List<Long> clothingIds) {
        try {
            List<Long> sortedIds = clothingIds.stream()
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            String input = userId + "|" + weather + "|" + personalColor + "|" + sortedIds;

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("해시 생성 실패", e);
            return UUID.randomUUID().toString();
        }
    }

    /**
     * 코디 제목 생성
     */
    private String generateCodyTitle(Clothing top, Clothing bottom, Clothing outer, Clothing shoe) {
        List<String> titleParts = new ArrayList<>();

        if (outer != null) titleParts.add(outer.getTitle());
        titleParts.add(top.getTitle());
        titleParts.add(bottom.getTitle());
        if (shoe != null) titleParts.add(shoe.getTitle());

        return String.join(" + ", titleParts);
    }

    /**
     * 코디 설명 생성
     */
    private String generateCodyDescription(Clothing top, Clothing bottom, Clothing outer, Clothing shoe, String weather) {
        StringBuilder desc = new StringBuilder();
        desc.append(getWeatherDescription(weather));

        Set<String> colors = new HashSet<>();
        colors.add(top.getColor());
        colors.add(bottom.getColor());
        if (outer != null) colors.add(outer.getColor());
        if (shoe != null) colors.add(shoe.getColor());

        desc.append(" ").append(String.join(", ", colors)).append(" 컬러 조합의 코디입니다.");
        return desc.toString();
    }

    /**
     * 날씨 설명 문구 반환
     */
    private String getWeatherDescription(String weather) {
        return WeatherType.fromString(weather).getDescription();
    }

    /**
     * 응답 생성
     */
    private CodyRecommendationResponse createResponse(List<Cody> codys) {
        List<CodyRecommendationResponse.RecommendedCody> recommendedCodys = codys.stream()
                .map(this::convertToCodyResponse)
                .toList();

        return new CodyRecommendationResponse(recommendedCodys, false);
    }

    /**
     * 폴백 응답 생성
     */
    private CodyRecommendationResponse createFallbackResponse() {
        return new CodyRecommendationResponse(new ArrayList<>(), true);
    }

    /**
     * Cody 엔티티를 응답 DTO로 변환
     */
    private CodyRecommendationResponse.RecommendedCody convertToCodyResponse(Cody cody) {
        long likeCount = cody.getLikes() != null ? cody.getLikes().size() : 0;
        List<CodyClothes> codyClothes = codyClothesRepository.findByCodyCodyId(cody.getCodyId());
        List<CodyRecommendationResponse.RecommendedCody.CodyItem> items = convertToCodyItems(codyClothes);

        return new CodyRecommendationResponse.RecommendedCody(
                cody.getCodyId(),
                cody.getWeather(),
                cody.getTitle(),
                cody.getDescription(),
                cody.getCreatedAt(),
                likeCount,
                items
        );
    }

    /**
     * 코디에 포함된 의상들을 CodyItem 리스트로 변환
     */
    private List<CodyRecommendationResponse.RecommendedCody.CodyItem> convertToCodyItems(List<CodyClothes> codyClothes) {
        return codyClothes.stream()
                .map(cc -> {
                    Clothing clothing = cc.getClothing();
                    return new CodyRecommendationResponse.RecommendedCody.CodyItem(
                            clothing.getClothingId(),
                            clothing.getCategory(),
                            clothing.getTitle(),
                            clothing.getColor(),
                            clothing.getImageUrl(),
                            clothing.getBrand()
                    );
                })
                .toList();
    }

    /**
     * 후보 의상 정보 로깅
     */
    private void logCandidatesSummary(ClothingCandidates candidates) {
        log.info("후보 의상 수 - TOP: {}, BOTTOM: {}, OUTER: {}, SHOES: {}",
                candidates.tops.size(), candidates.bottoms.size(),
                candidates.outers.size(), candidates.shoes.size());
    }

    /**
     * 후보 의상들을 담는 내부 클래스
     */
    private static class ClothingCandidates {
        List<Clothing> tops = new ArrayList<>();
        List<Clothing> bottoms = new ArrayList<>();
        List<Clothing> outers = new ArrayList<>();
        List<Clothing> shoes = new ArrayList<>();

        boolean isEmpty() {
            return tops.isEmpty() || bottoms.isEmpty();
        }
    }

    /**
     * 의상 카테고리 열거형
     */
    private enum ClothingCategory {
        TOP, BOTTOM, OUTER, SHOES
    }

    /**
     * 날씨 타입 열거형
     */
    private enum WeatherType {
        RAIN("비 오는 날씨에 적합한"),
        COLD("추운 날씨에 따뜻한"),
        HOT("더운 날씨에 시원한"),
        SUNNY("맑은 날씨에 어울리는"),
        CLOUDY("흐린 날씨에 어울리는");

        private final String description;

        WeatherType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean matches(String weather) {
            return this.name().equalsIgnoreCase(weather);
        }

        public static WeatherType fromString(String weather) {
            try {
                return valueOf(weather.toUpperCase());
            } catch (IllegalArgumentException e) {
                return SUNNY; // 기본값
            }
        }
    }
}
