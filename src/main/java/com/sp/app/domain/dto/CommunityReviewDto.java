package com.sp.app.domain.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityReviewDto {
    private long reviewId;
    private long recipeId; // 어떤 레시피를 따라 만들었는지 연결하는 번호
    private long memberId;

    private String title;
    private String content;
    private String imageUrl;
    private Integer status;
    private String createdAt;
    private String updatedAt;

    // 작성자 이름
    private String memberName;

    // 따라 만든 원본 레시피 정보
    private String recipeTitle;
    private String recipeThumbnailUrl;
    private boolean originalActive; // 원본 레시피가 공개 상태인지 확인해서 링크를 표시하는 값

    // 사진 업로드와 삭제 여부
    private MultipartFile selectFile;
    private boolean deleteImage;

    // 본인 후기의 수정·삭제 버튼 표시
    private boolean canEdit;
    private boolean canDelete;
}