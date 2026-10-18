package com.sp.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.app.domain.dto.MyPreferencesDto;
import com.sp.app.domain.dto.MyRecipeDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MyPreferencesService;
import com.sp.app.service.MyRecipeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/my")
public class MyKitchenController {
	private final MyRecipeService myRecipeService;
    private final MyPreferencesService myPreferencesService;
	
	private Long getLoginMemberId() { //데이터 주인 확인용
        SessionInfo info = LoginMemberUtil.getSessionInfo();
        if (info == null || info.getMember_id() == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }
        return info.getMember_id();
    }

    // 1. 내 냉장고 (/my 또는 /my/fridge)
    @GetMapping({"", "/", "/fridge"})
    public String fridge(Model model) {
        model.addAttribute("tab", "fridge");
        model.addAttribute("pageTitle", "내 냉장고");
        model.addAttribute("pageDescription", "가지고 있는 재료를 관리하고 새로운 요리를 찾아보세요.");
        
        return "mykitchen/myfridge";
    }

    // 2. 내가 작성한 레시피
    @GetMapping("/recipes")
    public String myRecipes(Model model,
    		@RequestParam(name="page", defaultValue="1") int page,
    		@RequestParam(name="size", defaultValue="10") int size,
    		@RequestParam(name="sort", defaultValue="latest") String sort) {
        model.addAttribute("tab", "mine");
        model.addAttribute("pageTitle", "내가 작성한 레시피");
        
        Long memberId = getLoginMemberId();
		List<MyRecipeDto> list = null;
		int offset = (page-1) * size;
		
		list = myRecipeService.selectMyRecipes(memberId, offset, size, sort);
		long count = 0;
    		count = myRecipeService.countMyRecipes(memberId);
    		int totalPages = (int)Math.ceil((double)count/size);
    		
    		
    		model.addAttribute("recipes", list);
        model.addAttribute("count", count);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sort", sort);
        
        return "mykitchen/myrecipes";
    }

    // 3. 찜한 레시피
    @GetMapping("/bookmarks")
    public String bookmarks(Model model, 
    		@RequestParam(name="page", defaultValue="1") int page,
    		@RequestParam(name="size", defaultValue="10") int size,
    		@RequestParam(name="sort", defaultValue="latest") String sort) {
        model.addAttribute("tab", "favorites");
        model.addAttribute("pageTitle", "찜한 레시피");

        Long memberId = getLoginMemberId();
        List<MyRecipeDto> list = null; 
        int offset = (page-1) * size;
        
    		list = myRecipeService.selectBookmarkedRecipes(memberId, offset, size, sort);
    		long count = 0;
    		count = myRecipeService.countBookmarkedRecipes(memberId);
    		int totalPages = (int)Math.ceil((double)count/size);
    		
    		model.addAttribute("recipes", list);
        model.addAttribute("count", count);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sort", sort);
    	
    	return "mykitchen/myrecipes";
    }

    // 4. 알림
    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("tab", "notifications");
        model.addAttribute("pageTitle", "내 알림");
        
        return "mykitchen/mynotifications";
    }

    // 5. 관심사 설정 (페이지 조회)
    @GetMapping("/preferences")
    public String preferences(Model model) {
        Long memberId = getLoginMemberId();
        
        MyPreferencesDto preferences = myPreferencesService.getMemberPreferences(memberId);

        model.addAttribute("tab", "tags");
        model.addAttribute("pageTitle", "관심 태그 & 카테고리");
        model.addAttribute("categories", preferences.getCategories());
        model.addAttribute("tags", preferences.getTags());

        return "mykitchen/mytags";
    }

    // 5-1. 관심사 설정 (저장 - REST API)
    @PutMapping("/preferences")
    @ResponseBody
    public ResponseEntity<?> updatePreferences(@RequestBody MyPreferencesDto dto) {
        Long memberId = getLoginMemberId();
        
        myPreferencesService.updateMemberPreferences(memberId, dto);
        
        return ResponseEntity.ok(Map.of("message", "관심 설정이 성공적으로 저장되었습니다."));
    }

    // 6. 쪽지
    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("tab", "messages");
        model.addAttribute("pageTitle", "쪽지함");

        return "mykitchen/mymessages";
    }

    // 7. 신고 / 문의
    @GetMapping("/inquiries")
    public String inquiries(Model model) {
        model.addAttribute("tab", "inquiries");
        model.addAttribute("pageTitle", "신고 / 문의");
        
        return "mykitchen/myinquiries";
    }

    // 8. 나의 활동
    @GetMapping("/activity")
    public String activity(Model model) {
        model.addAttribute("tab", "activity");
        model.addAttribute("pageTitle", "나의 활동");
        
        return "mykitchen/myactivity";
    }
}