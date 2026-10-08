package com.sp.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.admin.repository.MyIngredientRepository;
import com.sp.app.domain.dto.MyIngredientSearchDto;
import com.sp.app.domain.entity.MyIngredient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyIngredientServiceImpl implements MyIngredientService {
	private final MyIngredientRepository ingredientRepository;

    public List<MyIngredientSearchDto> searchIngredients(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        String trimmedKeyword = keyword.trim();

        List<MyIngredient> ingredients =
                ingredientRepository.searchIngredients(trimmedKeyword);

        return ingredients.stream()
                .map(MyIngredientSearchDto::from)
                .toList();
    }
}