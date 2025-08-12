package com.rofix.fitspot.webservice.api.user.util;

import java.util.*;

public class ColorMatchingUtil {

    // WARM 톤에 어울리는 색상들
    private static final Set<String> WARM_COLORS = new HashSet<>(Arrays.asList(
            "RED", "ORANGE", "YELLOW", "BROWN", "GOLD", "CREAM", "IVORY", "BEIGE",
            "CORAL", "PEACH", "TOMATO", "RUST", "BURGUNDY", "MAROON"
    ));

    // COOL 톤에 어울리는 색상들
    private static final Set<String> COOL_COLORS = new HashSet<>(Arrays.asList(
            "BLUE", "PURPLE", "GREEN", "PINK", "GRAY", "BLACK", "WHITE", "NAVY",
            "MINT", "LAVENDER", "SILVER", "EMERALD", "TEAL", "INDIGO"
    ));

    // 중성 색상 (모든 퍼스널 컬러에 어울림)
    private static final Set<String> NEUTRAL_COLORS = new HashSet<>(Arrays.asList(
            "BLACK", "WHITE", "GRAY", "NAVY", "BEIGE"
    ));

    /**
     * 퍼스널 컬러와 의상 색상이 매칭되는지 확인
     * @param personalColor 사용자의 퍼스널 컬러 (WARM, COOL)
     * @param clothingColor 의상 색상
     * @return 매칭 점수 (높을수록 잘 어울림)
     */
    public static int calculateColorMatchScore(String personalColor, String clothingColor) {
        if (personalColor == null || clothingColor == null) {
            return 0;
        }

        String upperPersonalColor = personalColor.toUpperCase();
        String upperClothingColor = clothingColor.toUpperCase();

        // 중성 색상은 모든 퍼스널 컬러에 높은 점수
        if (NEUTRAL_COLORS.contains(upperClothingColor)) {
            return 8;
        }

        // 완벽 매칭
        if ("WARM".equals(upperPersonalColor) && WARM_COLORS.contains(upperClothingColor)) {
            return 10;
        }
        if ("COOL".equals(upperPersonalColor) && COOL_COLORS.contains(upperClothingColor)) {
            return 10;
        }

        // 부분 매칭 (반대 톤이지만 일부 색상은 괜찮음)
        if ("WARM".equals(upperPersonalColor) && COOL_COLORS.contains(upperClothingColor)) {
            return 3;
        }
        if ("COOL".equals(upperPersonalColor) && WARM_COLORS.contains(upperClothingColor)) {
            return 3;
        }

        // 기본 점수
        return 5;
    }

    /**
     * 색상이 퍼스널 컬러와 매칭되는지 확인 (임계값 기준)
     */
    public static boolean isColorMatched(String personalColor, String clothingColor) {
        return calculateColorMatchScore(personalColor, clothingColor) >= 5;
    }
}