package com.sp.app.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyRecipeRecommendDto {
    private Long recipeId;
    private String title;
    private String thumbnailUrl;
    private int totalMatchCount;   // 보유 재료 일치 개수
    private int urgentMatchCount;  // 임박/경과 재료 일치 개수
    private double matchScore;     // 최종 가중치 점수

    private double matchRate;                   // 일치율 (%)
    private List<String> matchingIngredients;   // 보유 일치 재료명 리스트
    private List<String> missingIngredients;    // 부족한 재료명 리스트
    private List<String> urgentIngredients; //임박재료
}
