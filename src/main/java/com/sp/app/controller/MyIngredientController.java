package com.sp.app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.MyIngredientSearchDto;
import com.sp.app.service.MyIngredientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/myingredients")
@RequiredArgsConstructor
public class MyIngredientController {
	private final MyIngredientService ingredientService;

    @GetMapping("/search")
    public List<MyIngredientSearchDto> searchIngredients(
            @RequestParam String keyword
    ) {
        return ingredientService.searchIngredients(keyword);
    }
}
