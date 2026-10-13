package com.sp.app.domain.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityRecipeStepDto {
    private long stepId;             // 조리 단계 번호
    private long recipeId;           // 레시피 번호
    private int stepNo;              // 조리 순서: 1, 2, 3...

    private String description;      // 조리 방법
    private String imageUrl;         // 단계 사진 경로
    private MultipartFile selectFile; // 단계 사진 업로드

    private Integer timerSeconds;    // 타이머 시간(초). 없으면 null
}