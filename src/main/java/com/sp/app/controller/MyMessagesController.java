package com.sp.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.app.domain.dto.MyMessagesDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MyMessagesService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MyMessagesController {

    private final MyMessagesService myMessagesService;

    private Long getLoginMemberId() { 
        SessionInfo info = LoginMemberUtil.getSessionInfo();
        if (info == null || info.getMember_id() == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }
        return info.getMember_id();
    }

    @GetMapping("/mymessages")
    public String kitchenPage(
            @RequestParam(name = "tab", defaultValue = "messages") String tab,
            @RequestParam(name = "box", defaultValue = "received") String box,
            Model model) {

        Long currentUserId = getLoginMemberId();
        List<MyMessagesDto.Response> messages;

        if ("sent".equals(box)) {
            messages = myMessagesService.getSentMessages(currentUserId);
        } else {
            messages = myMessagesService.getReceivedMessages(currentUserId);
        }

        model.addAttribute("messages", messages);
        model.addAttribute("box", box);
        model.addAttribute("tab", tab);

        return "mykitchen/mymessages"; 
    }

    // 회원 닉네임 검색 API
    @GetMapping("/messages/members")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchMembers(@RequestParam("keyword") String keyword) {
        List<MyMessagesDto.MemberSearchResult> members = myMessagesService.searchMembers(keyword);
        return ResponseEntity.ok(Map.of("items", members));
    }

    // 쪽지 발송 API
    @PostMapping("/messages")
    @ResponseBody
    public ResponseEntity<Void> sendMessage(@RequestBody MyMessagesDto.SendRequest request) {
        myMessagesService.sendMessages(getLoginMemberId(), request);
        return ResponseEntity.ok().build();
    }

    // 쪽지 상세 조회 API
    @GetMapping("/messages/{id}")
    @ResponseBody
    public ResponseEntity<MyMessagesDto.Response> getMessage(@PathVariable("id") Long id) {
        return ResponseEntity.ok(myMessagesService.getMessageDetail(id, getLoginMemberId()));
    }

    // 쪽지 읽음 처리 API
    @PatchMapping("/messages/{id}/read")
    @ResponseBody
    public ResponseEntity<Void> readMessage(@PathVariable("id") Long id) {
        myMessagesService.readMessage(id, getLoginMemberId());
        return ResponseEntity.ok().build();
    }

    // 쪽지 삭제 API (Soft Delete: 내 쪽지함에서만 숨김)
    @DeleteMapping("/messages/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteMessage(@PathVariable("id") Long id) {
        myMessagesService.deleteMessage(id, getLoginMemberId());
        return ResponseEntity.ok().build();
    }

    //발송 취소 API (Hard Delete: 상대방이 안 읽었을 때 DB에서 완전 삭제)
    @DeleteMapping("/messages/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Void> cancelMessage(@PathVariable("id") Long id) {
        myMessagesService.cancelMessage(id, getLoginMemberId());
        return ResponseEntity.ok().build();
    }
}