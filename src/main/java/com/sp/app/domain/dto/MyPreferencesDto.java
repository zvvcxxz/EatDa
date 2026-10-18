package com.sp.app.domain.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyPreferencesDto {

    // 회원 및 요청 전달용 필드
    private Long memberId;
    private List<Long> categoryIds; // 저장 시 선택된 카테고리 ID 목록
    private List<Long> tagIds;      // 저장 시 선택된 태그 ID 목록

    // 화면 조회용 목록 데이터
    private List<CategoryItem> categories;
    private List<TagItem> tags;

    //카테고리 항목
    @Getter @Setter @NoArgsConstructor
    public static class CategoryItem {
        private Long categoryId;
        private String categoryCode;
        private String categoryName;
        private String description;
        private boolean selected; // 해당 회원의 선택 여부
    }

    //태그 항목
    @Getter @Setter @NoArgsConstructor
    public static class TagItem {
        private Long tagId;
        private String tagName;
        private String tagGroup;
        private boolean selected; 
    }
}