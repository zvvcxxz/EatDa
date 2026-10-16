package com.sp.app.admin.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRecipeDto {
	private Long recipeId;
	private Long memberId;
	private String title;
	private String summary;
	private String thumbnail;
	private int viewCount;
	
	private String sourceType;
	private int status;
	private String blindReason;
	
	private String createdAt;
	private String updateAt;
	
	private String loginId;
	private String userName;
	
    private String thumbnailUrl;
    private Integer cookMinutes;
    private String difficulty;
    private Integer servings;
}
