package com.sp.app.admin.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminIngredientDto {
	private long ingredientId;
	private String ingredientName;
	private String ingredientType;
	private String defaultUnit;
	private String activeYn;
	private String createdAt;
	private String updatedAt;
}
