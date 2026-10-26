package com.sp.app.controller;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.sp.app.domain.dto.MemberDto;
import com.sp.app.domain.dto.MyMessagesDto;
import com.sp.app.domain.dto.MyPreferencesDto;
import com.sp.app.domain.dto.MyRecipeDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MemberService;
import com.sp.app.service.MyMessagesService;
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
    private final MyMessagesService myMessagesService;
    private final MemberService memberService;
	
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

    // 4. 레시피 추천
    @GetMapping("/myRecipeAi")
    public String notifications(Model model) {
        model.addAttribute("tab", "myRecipeAi");
        model.addAttribute("pageTitle", "냉장고를 털어봐");
        
        return "mykitchen/myRecipeAi";
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
    public String kitchenPage(
            @RequestParam(name = "tab", defaultValue = "messages") String tab,
            @RequestParam(name = "box", defaultValue = "received") String box,
            Model model) {

        Long currentUserId = getLoginMemberId();

        List<MyMessagesDto.Response> sentMessages = myMessagesService.getSentMessages(currentUserId);
        List<MyMessagesDto.Response> receivedMessages = myMessagesService.getReceivedMessages(currentUserId);

        model.addAttribute("sentMessages", sentMessages);
        model.addAttribute("receivedMessages", receivedMessages);
        model.addAttribute("box", box);
        model.addAttribute("tab", tab);

        return "mykitchen/mymessages"; 
    }

    //7.채팅
    @GetMapping("/chat")
    public String chatPage(@RequestParam(name = "roomId", required = false) Long roomId, Model model) {
        try {
            Long currentMemberId = getLoginMemberId();
            MemberDto currentMember = memberService.findById(currentMemberId);
            
            model.addAttribute("currentMemberId", currentMemberId);
            model.addAttribute("currentMemberName", currentMember != null ? currentMember.getName() : "나");

            List<Map<String, Object>> chatRooms = new ArrayList<>();

            // 1. 현재 선택된 roomId가 있으면 해당 방 정보를 목록에 추가 (DB 연동 전 임시 구성)
            if (roomId != null) {
                model.addAttribute("selectedRoomId", roomId);
                
                // 상대방 ID 계산
                long val1 = roomId / 10000;
                long val2 = roomId % 10000;
                Long targetMemberId = (val1 == currentMemberId) ? val2 : val1;

                MemberDto targetMember = memberService.findById(targetMemberId);
                String targetNickname = (targetMember != null) ? targetMember.getName() : "알 수 없는 사용자";

                Map<String, Object> currentRoom = new HashMap<>();
                currentRoom.put("roomId", roomId);
                currentRoom.put("targetMemberId", targetMemberId); // 상대방 ID 추가
                currentRoom.put("targetNickname", targetNickname);
                model.addAttribute("currentRoom", currentRoom);

                // 좌측 목록용 데이터 객체 추가
                Map<String, Object> roomItem = new HashMap<>();
                roomItem.put("roomId", roomId);
                roomItem.put("targetNickname", targetNickname);
                roomItem.put("lastMessage", "대화를 시작하세요.");
                roomItem.put("lastMessageTime", "방금 전");
                roomItem.put("unreadCount", 0);
                chatRooms.add(roomItem);
            }

            // TODO: 실제 DB 구현 시 chatService.listChatRooms(currentMemberId) 결과를 넣으시면 됩니다.
            model.addAttribute("chatRooms", chatRooms);

            return "mykitchen/mychat";
        } catch (IllegalStateException e) {
            return "redirect:/member/login";
        }
    }
    
    // 8. 신고 / 문의
    @GetMapping("/inquiries")
    public String inquiries(Model model) {
        model.addAttribute("tab", "inquiries");
        model.addAttribute("pageTitle", "신고 / 문의");
        
        return "mykitchen/myinquiry";
    }    

    // 9. ai추천
    @GetMapping("/recipeai")
    public String activity(Model model) {
        model.addAttribute("tab", "recipeai");
        model.addAttribute("pageTitle", "냉장고를 털어봐");
        
        return "mykitchen/myRecipeAi";
    }
}