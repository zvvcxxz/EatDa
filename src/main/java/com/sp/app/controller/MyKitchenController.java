package com.sp.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/my")
public class MyKitchenController {

    // 1. 내 냉장고 (/my 또는 /my/fridge)
    @GetMapping({"", "/", "/fridge"})
    public String fridge(Model model) {
        model.addAttribute("tab", "fridge");
        model.addAttribute("pageTitle", "내 냉장고");
        model.addAttribute("pageDescription", "가지고 있는 재료를 관리하고 새로운 요리를 찾아보세요.");
        
        // TODO: 냉장고 재료 목록 등 조회 로직
        return "mykitchen/myfridge";
    }

    // 2. 내가 작성한 레시피
    @GetMapping("/recipes")
    public String myRecipes(Model model) {
        model.addAttribute("tab", "mine");
        model.addAttribute("pageTitle", "내가 작성한 레시피");
        
        // TODO: 내가 작성한 레시피 목록 조회 로직
        return "mykitchen/myrecipes";
    }

    // 3. 찜한 레시피
    @GetMapping("/bookmarks")
    public String bookmarks(Model model) {
        model.addAttribute("tab", "favorites");
        model.addAttribute("pageTitle", "찜한 레시피");
       
        // TODO: 찜한 레시피 목록 조회 로직
        return "mykitchen/myrecipes";
    }

    // 4. 알림
    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("tab", "notifications");
        model.addAttribute("pageTitle", "내 알림");
        
        // TODO: 알림 목록 조회 로직
        return "mykitchen/mynotifications";
    }

    // 5. 관심사 설정 (관심 태그)
    @GetMapping("/preferences")
    public String preferences(Model model) {
        model.addAttribute("tab", "tags");
        model.addAttribute("pageTitle", "관심 태그");
        
        // TODO: 관심 태그 목록 조회 로직
        return "mykitchen/mytags";
    }

    // 6. 쪽지
    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("tab", "messages");
        model.addAttribute("pageTitle", "쪽지함");
        
        // TODO: 쪽지 목록 조회 로직
        return "mykitchen/mymessages";
    }

    // 7. 신고 / 문의
    @GetMapping("/inquiries")
    public String inquiries(Model model) {
        model.addAttribute("tab", "inquiries");
        model.addAttribute("pageTitle", "신고 / 문의");
        
        // TODO: 신고/문의 목록 조회 로직
        return "mykitchen/myinquiries";
    }

    // 8. 나의 활동
    @GetMapping("/activity")
    public String activity(Model model) {
        model.addAttribute("tab", "activity");
        model.addAttribute("pageTitle", "나의 활동");
        
        // TODO: 월별 활동 통계 조회 로직
        return "mykitchen/myactivity";
    }
}