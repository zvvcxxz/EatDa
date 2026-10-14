package com.sp.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyRecipeRecommendDto {
    private Long recipeId;
    private String title;
    private String thumbnailUrl;
    private int totalMatchCount;   // 보유 재료 일치 개수
    private int urgentMatchCount;  // 임박/경과 재료 일치 개수
    private double matchScore;     // 최종 가중치 점수
}
