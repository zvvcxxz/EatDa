package com.sp.app.domain.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityRecipeDto {
    private long recipeId;            // 레시피 번호
    private Long authorId;            // API 레시피는 작성자가 없을 수 있음
    private String authorName;

    private String sourceType;        // API 또는 MEMBER
    private String externalKey;       // 외부 API의 레시피 식별값

    private String title;
    private String summary;           // 레시피 소개
    private String thumbnailUrl;      // 대표 이미지 경로
    private MultipartFile selectFile; // 대표 이미지 업로드

    private Integer cookMinutes;      // 조리 시간(분)
    private String difficulty;        // 난이도
    private Integer servings;         // 인분
    private String otherIngredient;   // 기타 재료

    private Integer status;           // 레시피 상태
    private String blindReason;       // 관리자 숨김 사유

    private String publishedAt;
    private String deletedAt;
    private String createdAt;
    private String updatedAt;

    // 선택한 카테고리와 태그 번호
    private List<Long> categoryIds;
    private List<Long> tagIds;

    // 화면에 표시할 개수
    private int likeCount;
    private int bookmarkCount;
    private int reviewCount;

    private boolean canEdit;
    private boolean canDelete;
}