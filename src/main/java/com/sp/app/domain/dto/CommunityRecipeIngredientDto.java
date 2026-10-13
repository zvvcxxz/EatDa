package com.sp.app.domain.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityRecipeIngredientDto {
    private long recipeId;          // 레시피 번호
    private long ingredientId;      // 재료 사전의 재료 번호
    private String ingredientName;  // 조회할 때 가져올 재료명

    private BigDecimal amount;      // 분량. 0.5처럼 소수도 저장
    private String unit;            // g, 개, 큰술 등
    private String requiredYn;      // Y: 필수, N: 선택 재료
    private int sortOrder;          // 재료 표시 순서
    private String note;            // 재료 설명
}