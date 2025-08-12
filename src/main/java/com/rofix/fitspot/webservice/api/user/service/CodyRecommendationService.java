package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.dto.CodyRecommendationRequest;
import com.rofix.fitspot.webservice.api.user.dto.CodyRecommendationResponse;
import com.rofix.fitspot.webservice.api.user.entity.*;
import com.rofix.fitspot.webservice.api.user.repository.ClothingRepository;
import com.rofix.fitspot.webservice.api.user.repository.CodyRepository;
import com.rofix.fitspot.webservice.api.user.repository.CodyClothesRepository;
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
            // 1. 기존 코디가 있는지 확인 (force=false인 경우)
            if (!request.getForce()) {
                Optional<Cody> existingCody = findExistingCody(request);
                if (existingCody.isPresent()) {
                    log.info("기존 코디 재사용: {}", existingCody.get().getCodyId());
                    return createResponse(List.of(existingCody.get()), false);
                }
            }

            // 2. 의상 후보 조회 및 필터링
            CandidateClothes candidates = getCandidateClothes(request);

            // 3. Fallback 체크
            if (candidates.tops.isEmpty() || candidates.bottoms.isEmpty()) {
                log.warn("충분한 의상이 없어 추천 실패 - TOP: {}, BOTTOM: {}",
                        candidates.tops.size(), candidates.bottoms.size());
                return new CodyRecommendationResponse(new ArrayList<>(), true);
            }

            // 4. 코디 조합 생성
            List<Cody> newCodys = generateCodyCombinations(request, candidates);

            log.info("코디 추천 완료 - 생성된 코디 수: {}", newCodys.size());
            return createResponse(newCodys, false);

        } catch (Exception e) {
            log.error("코디 추천 중 오류 발생", e);
            return new CodyRecommendationResponse(new ArrayList<>(), true);
        }
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
    private CandidateClothes getCandidateClothes(CodyRecommendationRequest request) {
        CandidateClothes candidates = new CandidateClothes();

        // TOP 카테고리 의상 조회
        candidates.tops = getFilteredClothes(request, "TOP");

        // BOTTOM 카테고리 의상 조회
        candidates.bottoms = getFilteredClothes(request, "BOTTOM");

        // OUTER 카테고리 의상 조회 (조건부)
        if (shouldIncludeOuter(request.getWeather())) {
            candidates.outers = getFilteredClothes(request, "OUTER");
        }

        return candidates;
    }

    /**
     * 카테고리별 필터링된 의상 조회
     */
    private List<Clothing> getFilteredClothes(CodyRecommendationRequest request, String category) {
        // 1. 날씨에 맞는 의상 우선 조회
        List<Clothing> clothes = clothingRepository.findByUserIdAndCategoryAndWeather(
                request.getUserId(), category, request.getWeather());

        // 2. 부족할 경우 전체 카테고리에서 추가 조회
        if (clothes.size() < 3) {
            List<Clothing> allCategoryClothes = clothingRepository.findByUserIdAndCategory(
                    request.getUserId(), category);

            // 중복 제거하여 추가
            Set<Long> existingIds = clothes.stream()
                    .map(Clothing::getClothingId)
                    .collect(Collectors.toSet());

            allCategoryClothes.stream()
                    .filter(c -> !existingIds.contains(c.getClothingId()))
                    .limit(5 - clothes.size())
                    .forEach(clothes::add);
        }

        // 3. 퍼스널 컬러 필터링 및 정렬
        return clothes.stream()
                .filter(clothing -> ColorMatchingUtil.isColorMatched(
                        request.getPersonalColor(), clothing.getColor()))
                .sorted((c1, c2) -> {
                    // 날씨 매칭 우선순위
                    int weatherScore1 = request.getWeather().equals(c1.getWeather()) ? 10 : 0;
                    int weatherScore2 = request.getWeather().equals(c2.getWeather()) ? 10 : 0;

                    // 색상 매칭 점수
                    int colorScore1 = ColorMatchingUtil.calculateColorMatchScore(
                            request.getPersonalColor(), c1.getColor());
                    int colorScore2 = ColorMatchingUtil.calculateColorMatchScore(
                            request.getPersonalColor(), c2.getColor());

                    int totalScore1 = weatherScore1 + colorScore1;
                    int totalScore2 = weatherScore2 + colorScore2;

                    if (totalScore1 != totalScore2) {
                        return Integer.compare(totalScore2, totalScore1); // 높은 점수 우선
                    }

                    // 최근 생성순
                    return c2.getCreatedAt().compareTo(c1.getCreatedAt());
                })
                .limit(5) // 상위 5개만 선택
                .collect(Collectors.toList());
    }

    /**
     * OUTER 포함 여부 결정
     */
    private boolean shouldIncludeOuter(String weather) {
        // 온도 고려 없이 날씨만으로 판단
        return "RAIN".equalsIgnoreCase(weather) || "COLD".equalsIgnoreCase(weather);
    }

    /**
     * 코디 조합 생성
     */
    private List<Cody> generateCodyCombinations(CodyRecommendationRequest request,
                                                CandidateClothes candidates) {
        List<Cody> generatedCodys = new ArrayList<>();

        // 최대 6가지 조합 생성
        int maxCombinations = Math.min(6, candidates.tops.size() * candidates.bottoms.size());

        for (int i = 0; i < maxCombinations && i < candidates.tops.size(); i++) {
            for (int j = 0; j < Math.min(2, candidates.bottoms.size()); j++) {
                Clothing top = candidates.tops.get(i);
                Clothing bottom = candidates.bottoms.get(j);
                Clothing outer = (!candidates.outers.isEmpty() && i < candidates.outers.size())
                        ? candidates.outers.get(i) : null;

                // 해시 생성으로 중복 확인
                String hash = generateHash(request.getUserId(), request.getWeather(), request.getPersonalColor(),
                        Arrays.asList(top.getClothingId(), bottom.getClothingId(),
                                outer != null ? outer.getClothingId() : null));

                Optional<Cody> existingCody = codyRepository.findByHash(hash);
                if (existingCody.isPresent()) {
                    generatedCodys.add(existingCody.get());
                    continue;
                }

                // 새 코디 생성
                Cody newCody = createNewCody(request, top, bottom, outer, hash);
                generatedCodys.add(newCody);

                if (generatedCodys.size() >= 3) break; // 최대 3개
            }
            if (generatedCodys.size() >= 3) break;
        }

        return generatedCodys;
    }

    /**
     * 새 코디 생성
     */
    private Cody createNewCody(CodyRecommendationRequest request, Clothing top, Clothing bottom,
                               Clothing outer, String hash) {
        User user = new User();
        user.setUserId(request.getUserId());

        Cody cody = new Cody();
        cody.setUser(user);
        cody.setWeather(request.getWeather());
        cody.setTitle(generateTitle(top, bottom, outer));
        cody.setDescription(generateDescription(top, bottom, outer, request.getWeather()));
        cody.setCreatedAt(LocalDateTime.now());
        cody.setHash(hash);

        // 코디 저장
        Cody savedCody = codyRepository.save(cody);

        // 코디-의상 관계 저장
        saveCodyClothes(cody, top);
        saveCodyClothes(cody, bottom);
        if (outer != null) {
            saveCodyClothes(cody, outer);
        }

        log.info("새 코디 생성 완료: {} - {}", savedCody.getCodyId(), savedCody.getTitle());
        return savedCody;
    }

    /**
     * 코디-의상 관계 저장
     */
    private void saveCodyClothes(Cody cody, Clothing clothing) {
        CodyClothes codyClothes = new CodyClothes();
        codyClothes.setCody(cody);
        codyClothes.setClothing(clothing);
        codyClothesRepository.save(codyClothes);
    }

    /**
     * 해시 생성
     */
    private String generateHash(Long userId, String weather, String personalColor, List<Long> clothingIds) {
        try {
            List<Long> sortedIds = clothingIds.stream()
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());

            String input = userId + "|" + weather + "|" + personalColor + "|" + sortedIds.toString();

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
     * 코디 제목 자동 생성
     */
    private String generateTitle(Clothing top, Clothing bottom, Clothing outer) {
        StringBuilder title = new StringBuilder();

        if (outer != null) {
            title.append(outer.getTitle()).append(" + ");
        }
        title.append(top.getTitle()).append(" + ").append(bottom.getTitle());

        return title.toString();
    }

    /**
     * 코디 설명 자동 생성
     */
    private String generateDescription(Clothing top, Clothing bottom, Clothing outer, String weather) {
        StringBuilder desc = new StringBuilder();
        desc.append(getWeatherDescription(weather));

        // 색상 조합
        Set<String> colors = new HashSet<>();
        colors.add(top.getColor());
        colors.add(bottom.getColor());
        if (outer != null) {
            colors.add(outer.getColor());
        }

        desc.append(" ").append(String.join(", ", colors)).append(" 컬러 조합의 코디입니다.");

        return desc.toString();
    }

    /**
     * 날씨 설명 문구 반환
     */
    private String getWeatherDescription(String weather) {
        switch (weather != null ? weather.toUpperCase() : "SUNNY") {
            case "RAIN": return "비 오는 날씨에 적합한";
            case "COLD": return "추운 날씨에 따뜻한";
            case "HOT": return "더운 날씨에 시원한";
            default: return "맑은 날씨에 어울리는";
        }
    }

    /**
     * 응답 생성
     */
    private CodyRecommendationResponse createResponse(List<Cody> codys, boolean fallback) {
        List<CodyRecommendationResponse.RecommendedCody> recommendedCodys = codys.stream()
                .map(this::convertToCodyResponse)
                .collect(Collectors.toList());

        return new CodyRecommendationResponse(recommendedCodys, fallback);
    }

    /**
     * Cody 엔티티를 응답 DTO로 변환
     */
    private CodyRecommendationResponse.RecommendedCody convertToCodyResponse(Cody cody) {
        // 좋아요 수 계산
        long likeCount = cody.getLikes() != null ? cody.getLikes().size() : 0;

        // 코디에 포함된 의상들 조회
        List<CodyClothes> codyClothes = codyClothesRepository.findByCodyCodyId(cody.getCodyId());
        List<CodyRecommendationResponse.RecommendedCody.CodyItem> items = codyClothes.stream()
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
                .collect(Collectors.toList());

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
     * 후보 의상들을 담는 내부 클래스
     */
    private static class CandidateClothes {
        List<Clothing> tops = new ArrayList<>();
        List<Clothing> bottoms = new ArrayList<>();
        List<Clothing> outers = new ArrayList<>();
    }
}