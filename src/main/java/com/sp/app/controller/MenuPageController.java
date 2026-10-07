package com.sp.app.controller;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

/** 화면 진입 경로만 제공. 각 담당자가 실제 컨트롤러를 만들면 해당 매핑을 이동하세요. */
public class MenuPageController {
 @GetMapping("/community/share") public String share(){return "community/share";}
 @GetMapping("/community/meet") public String meet(){return "community/meet";}
 private static final Map<String,String> TITLES=Map.ofEntries(
 Map.entry("/community/free","자유 이야기"),
 Map.entry("/community/recipes","모두의 레시피"),
 Map.entry("/community/cooking","따라 만든 요리"),
 Map.entry("/community/questions","요리 질문"),
 Map.entry("/my/fridge","내 냉장고"),
 Map.entry("/my/recipes","내가 쓴 레시피"),
 Map.entry("/my/bookmarks","찜한 레시피"),
 Map.entry("/my/notifications","알림"),
 Map.entry("/my/preferences","관심사 설정"),
 Map.entry("/my/messages","쪽지"),
 Map.entry("/missions","미션/포인트"),
 Map.entry("/shop","장보기"));
 @GetMapping({"/community/free","/community/recipes","/community/cooking","/community/questions","/my/fridge","/my/recipes","/my/bookmarks","/my/notifications","/my/preferences","/my/messages","/missions","/shop"})
 public String menu(HttpServletRequest request,Model model){model.addAttribute("pageTitle",TITLES.get(request.getServletPath()));return "main/menu";}
}
