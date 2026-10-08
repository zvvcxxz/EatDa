package com.sp.app.domain.dto;

import com.sp.app.domain.entity.MyIngredient;

public record MyIngredientSearchDto( Long ingredientId,
        String ingredientName, String ingredientType, String defaultUnit) {

    public static MyIngredientSearchDto from(MyIngredient ingredient) {
    	
        return new MyIngredientSearchDto(ingredient.getIngredientId(),
                ingredient.getIngredientName(), ingredient.getIngredientType(),
                ingredient.getDefaultUnit() );
    }
}