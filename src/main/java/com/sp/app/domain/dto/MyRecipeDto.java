package com.sp.app.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyRecipeDto {

    // 1. 레시피 기본 정보
    private Long recipeId;
    private Long authorId;
    private String authorName;          // 작성자 이름 (MEMBER.NAME)
    private String authorProfileUrl;    // 작성자 프로필 (MEMBER.PROFILE_URL)
    private String sourceType;          // API / MEMBER
    private String externalKey;
    private String title;
    private String summary;
    private String thumbnailUrl;
    private Integer cookMinutes;
    private String difficulty;
    private Integer servings;
    private Integer status;
    private String otherIngredient;     // 기타 재료
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 2. 통계 정보
    private int likeCount;              // 좋아요 수
    private int bookmarkCount;          // 찜 수
    private int reviewCount;            // 후기 수

    // 3. 연관 세부 데이터 리스트
    private List<IngredientItem> ingredients; // 레시피 재료
    private List<StepItem> steps;             // 조리 단계
    private List<CategoryItem> categories;     // 카테고리
    private List<TagItem> tags;               // 태그
    private List<ReviewItem> reviews;         // 따라 만든 후기

    
    //Inner DTO 
    // 레시피 재료 정보
    @Getter @Setter @NoArgsConstructor
    public static class IngredientItem {
        private Long ingredientId;
        private String ingredientName;
        private String ingredientType;  // 카테고리 (육류, 채소 등)
        private String categoryEmoji;   // 윈도우 이모지
        private BigDecimal amount;
        private String unit;
        private String requiredYn;      // 필수 여부 (Y/N)
        private Integer sortOrder;
        private String note;
    }

    // 조리 단계 정보
    @Getter @Setter @NoArgsConstructor
    public static class StepItem {
        private Long stepId;
        private Integer stepNo;
        private String description;
        private String imageUrl;
        private Integer timerSeconds;
    }

    // 카테고리 정보
    @Getter @Setter @NoArgsConstructor
    public static class CategoryItem {
        private Long categoryId;
        private String categoryCode;
        private String categoryName;
    }

    // 태그 정보
    @Getter @Setter @NoArgsConstructor
    public static class TagItem {
        private Long tagId;
        private String tagName;
        private String tagGroup;
    }

    // 따라 만든 후기 정보
    @Getter @Setter @NoArgsConstructor
    public static class ReviewItem {
        private Long reviewId;
        private Long memberId;
        private String reviewerName;
        private String reviewerProfileUrl;
        private String title;
        private String content;
        private String imageUrl;
        private LocalDateTime createdAt;
    }
}