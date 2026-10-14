package com.sp.app.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.sp.app.domain.entity.MemberFridge;
import com.sp.app.domain.entity.MyIngredient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MyFridgeDto {

    // 1. 냉장고 재료 등록 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class RegisterRequest {
        private Long ingredientId;
        private BigDecimal quantity;
        private String unit;
        private LocalDate expiresOn;
        private String memo;
    }

    // 2. 냉장고 재료 수정 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateRequest {
        private BigDecimal quantity;
        private String unit;
        private LocalDate expiresOn;
        private String memo;
    }

    // 3. 재료 사전 검색 / 카테고리별 조회 응답 DTO
    @Getter
    public static class IngredientResponse {
        private final Long ingredientId;
        private final String ingredientName;
        private final String defaultUnit;
        private final String type;
        private final String categoryEmoji;

        public IngredientResponse(MyIngredient ingredient) {
            this.ingredientId = ingredient.getId();
            this.ingredientName = ingredient.getName();
            this.defaultUnit = ingredient.getDefaultUnit();
            this.type = ingredient.getType();
            this.categoryEmoji = MyFridgeDto.getCategoryEmoji(ingredient.getType());
        }							//getter충돌 방지
    }

    // 4. 내 냉장고 보유 재료 목록 조회 응답 DTO
    @Getter
    public static class ItemResponse {
        private final Long fridgeItemId;
        private final Long ingredientId;
        private final String ingredientName;
        private final String categoryCode;
        private final String categoryName;
        private final String categoryEmoji;
        private final BigDecimal quantity;
        private final String unit;
        private final LocalDate expiresOn;
        private final LocalDate registeredDate;
        private final String memo;

        // 유통기한 계산 필드
        private final boolean expired;   // 유통기한 경과 여부
        private final boolean expiring;  // 유통기한 임박 여부 (D-3 이내)
        private final Long dDay;         // D-Day

        public ItemResponse(MemberFridge item) {
            this.fridgeItemId = item.getId();
            this.ingredientId = item.getIngredient().getId();
            this.ingredientName = item.getIngredient().getName();
            
            this.categoryCode = item.getIngredient().getType();
            this.categoryName = MyFridgeDto.getCategoryLabel(this.categoryCode);
            this.categoryEmoji = MyFridgeDto.getCategoryEmoji(this.categoryCode);

            this.quantity = item.getQuantity();
            this.unit = item.getUnit();
            this.expiresOn = item.getExpiresOn();
            this.registeredDate = item.getCreatedAt().toLocalDate();
            this.memo = item.getMemo();

            LocalDate today = LocalDate.now();
            if (this.expiresOn != null) {
                this.dDay = ChronoUnit.DAYS.between(today, this.expiresOn);
                this.expired = this.expiresOn.isBefore(today);
                this.expiring = !this.expired && (this.dDay >= 0 && this.dDay <= 3);
            } else {
                this.dDay = null;
                this.expired = false;
                this.expiring = false;
            }
        }
    }

    // 카테고리 이모지 매핑 
    public static String getCategoryEmoji(String code) {
        if (code == null) return "📦";
        return switch (code.trim().toUpperCase()) {
            case "MEAT", "육류" -> "🥩";
            case "SEAFOOD", "해산물" -> "🐟";
            case "EGG", "난류" -> "🥚";
            case "DAIRY", "유제품" -> "🥛";
            case "GRAIN", "곡물·면류" -> "🌾";
            case "VEGETABLE", "채소" -> "🥬";
            case "FRUIT", "과일" -> "🍎";
            case "MUSHROOM", "버섯" -> "🍄";
            case "NUT", "콩·견과류" -> "🫘";
            case "PROCESSED", "가공식품" -> "🥫";
            case "OIL", "유지류" -> "🧈";
            case "SEASONING", "조미료" -> "🧂";
            case "WATER", "물·육수" -> "💧";
            default -> "📦";
        };
    }

    // 카테고리 한글 라벨 매핑용
    public static String getCategoryLabel(String code) {
        if (code == null) return "기타";
        return switch (code.trim().toUpperCase()) {
            case "MEAT", "육류" -> "육류";
            case "SEAFOOD", "해산물" -> "해산물";
            case "EGG", "난류" -> "난류";
            case "DAIRY", "유제품" -> "유제품";
            case "GRAIN", "곡물·면류" -> "곡물·면류";
            case "VEGETABLE", "채소" -> "채소";
            case "FRUIT", "과일" -> "과일";
            case "MUSHROOM", "버섯" -> "버섯";
            case "NUT", "콩·견과류" -> "콩·견과류";
            case "PROCESSED", "가공식품" -> "가공식품";
            case "OIL", "유지류" -> "유지류";
            case "SEASONING", "조미료" -> "조미료";
            case "WATER", "물·육수" -> "물·육수";
            default -> code; // 매핑되지 않는 문자열이어도 null/기타 대신 해당 값 그대로 출력
        };
    }
}