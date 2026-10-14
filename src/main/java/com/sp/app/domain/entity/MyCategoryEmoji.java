package com.sp.app.domain.entity;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MyCategoryEmoji {
	MEAT("MEAT", "육류", "🥩"),
    SEAFOOD("SEAFOOD", "해산물", "🐟"),
    EGG("EGG", "난류", "🥚"),
    DAIRY("DAIRY", "유제품", "🥛"),
    GRAIN("GRAIN", "곡물·면류", "🌾"),
    VEGETABLE("VEGETABLE", "채소", "🥬"),
    FRUIT("FRUIT", "과일", "🍎"),
    MUSHROOM("MUSHROOM", "버섯", "🍄"),
    NUT("NUT", "콩·견과류", "🫘"),
    PROCESSED("PROCESSED", "가공식품", "🥫"),
    OIL("OIL", "유지류", "🧈"),
    SEASONING("SEASONING", "조미료", "🧂"),
    WATER("WATER", "물·육수", "💧"),
    ETC("ETC", "기타", "📦");

    private final String code;
    private final String label;
    private final String emoji;

    public static String getEmojiByCode(String code) {
        if (code == null) return ETC.getEmoji();
        return Arrays.stream(values())
                .filter(cat -> cat.getCode().equalsIgnoreCase(code))
                .findFirst()
                .map(MyCategoryEmoji::getEmoji)
                .orElse(ETC.getEmoji());
    }

    public static String getLabelByCode(String code) {
        if (code == null) return ETC.getLabel();
        return Arrays.stream(values())
                .filter(cat -> cat.getCode().equalsIgnoreCase(code))
                .findFirst()
                .map(MyCategoryEmoji::getLabel)
                .orElse(ETC.getLabel());
    }
}
