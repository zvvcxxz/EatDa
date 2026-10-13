package com.sp.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 디자인 확인용. 실제 컨트롤러를 만들 때 해당 주소를 옮기고 이 매핑은 제거
@Controller
@RequestMapping("/community")
public class CommunityDesignController {
    @GetMapping("/recipes")
    public String recipes(Model model) {
        model.addAttribute("pageTitle", "모두의 레시피");
        model.addAttribute("activeMenu", "recipes");
        model.addAttribute("previewId", 1L);
        return "community/communityRecipe-list";
    }

    @GetMapping("/recipes/write")
    public String recipeWrite(Model model) {
        model.addAttribute("pageTitle", "레시피 작성");
        model.addAttribute("activeMenu", "recipes");
        model.addAttribute("previewId", 1L);
        return "community/communityRecipe-write";
    }

    @GetMapping("/recipes/{recipeId}")
    public String recipeDetail(@PathVariable("recipeId") long id, Model model) {
        model.addAttribute("pageTitle", "레시피 전체보기");
        model.addAttribute("activeMenu", "recipes");
        model.addAttribute("previewId", id);
        return "community/communityRecipe-detail";
    }

    @GetMapping("/recipes/{recipeId}/update")
    public String recipeUpdate(@PathVariable("recipeId") long id, Model model) {
        model.addAttribute("pageTitle", "레시피 수정");
        model.addAttribute("activeMenu", "recipes");
        model.addAttribute("previewId", id);
        return "community/communityRecipe-update";
    }

    @GetMapping("/recipes/{recipeId}/mode")
    public String recipeMode(@PathVariable("recipeId") long id, Model model) {
        model.addAttribute("pageTitle", "요리모드");
        model.addAttribute("activeMenu", "recipes");
        model.addAttribute("previewId", id);
        return "community/communityRecipe-mode";
    }

    @GetMapping("/cooking")
    public String reviews(Model model) {
        model.addAttribute("pageTitle", "따라 만든 요리");
        model.addAttribute("activeMenu", "cooking");
        model.addAttribute("previewId", 1L);
        return "community/communityReview-list";
    }

    @GetMapping("/cooking/write")
    public String reviewWrite(@RequestParam(name = "recipeId", defaultValue = "0") long id, Model model) {
        model.addAttribute("pageTitle", "후기 작성");
        model.addAttribute("activeMenu", "cooking");
        model.addAttribute("previewId", id);
        return "community/communityReview-write";
    }

    @GetMapping("/cooking/{reviewId}")
    public String reviewDetail(@PathVariable("reviewId") long id, Model model) {
        model.addAttribute("pageTitle", "따라 만든 후기");
        model.addAttribute("activeMenu", "cooking");
        model.addAttribute("previewId", id);
        return "community/communityReview-detail";
    }

    @GetMapping("/cooking/{reviewId}/update")
    public String reviewUpdate(@PathVariable("reviewId") long id, Model model) {
        model.addAttribute("pageTitle", "후기 수정");
        model.addAttribute("activeMenu", "cooking");
        model.addAttribute("previewId", id);
        return "community/communityReview-update";
    }

    @GetMapping("/questions")
    public String questions(Model model) {
        model.addAttribute("pageTitle", "요리 질문");
        model.addAttribute("activeMenu", "questions");
        model.addAttribute("previewId", 1L);
        return "community/communityQuestion-list";
    }

    @GetMapping("/questions/write")
    public String questionWrite(Model model) {
        model.addAttribute("pageTitle", "질문 작성");
        model.addAttribute("activeMenu", "questions");
        model.addAttribute("previewId", 1L);
        return "community/communityQuestion-write";
    }

    @GetMapping("/questions/{questionId}")
    public String questionDetail(@PathVariable("questionId") long id, Model model) {
        model.addAttribute("pageTitle", "질문 상세");
        model.addAttribute("activeMenu", "questions");
        model.addAttribute("previewId", id);
        return "community/communityQuestion-detail";
    }

    @GetMapping("/questions/{questionId}/update")
    public String questionUpdate(@PathVariable("questionId") long id, Model model) {
        model.addAttribute("pageTitle", "질문 수정");
        model.addAttribute("activeMenu", "questions");
        model.addAttribute("previewId", id);
        return "community/communityQuestion-update";
    }
}
