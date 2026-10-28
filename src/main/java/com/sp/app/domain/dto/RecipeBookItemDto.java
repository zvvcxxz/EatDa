package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.Setter;

// 레시피북 : 목록에 필요한 기본 정보
@Getter
@Setter
public class RecipeBookItemDto {
    private String id;
    private String title;
    private String image;
    private String category;
    private String method;
}
