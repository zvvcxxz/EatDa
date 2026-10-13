package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityIngredientDto {
    private long ingredientId;     // 재료 번호
    private String ingredientName; // 재료명
    private String ingredientType; // 육류, 채소, 조미료 등
    private String defaultUnit;    // 기본 단위: g, 개 등

    private String activeYn;       // Y: 사용, N: 미사용
    private String createdAt;
    private String updatedAt;
}