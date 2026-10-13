package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityCategoryDto {
    private long categoryId;      // 카테고리 번호
    private String categoryCode;  // 카테고리 구분 코드
    private String categoryName;  // 화면에 표시할 이름
    private String description;

    private String activeYn;      // Y: 사용, N: 미사용
    private int sortOrder;        // 화면 표시 순서

    private String createdAt;
    private String updatedAt;
}