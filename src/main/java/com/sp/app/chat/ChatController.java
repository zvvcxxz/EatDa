package com.sp.app.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.app.domain.dto.MemberDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final UserSessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final MemberService memberService;

    private SessionInfo getLoginMember() {
        SessionInfo info = LoginMemberUtil.getSessionInfo();
        if (info == null || info.getMember_id() == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }
        return info;
    }

    @GetMapping("/mychat/searchMember")
    @ResponseBody
    public ResponseEntity<List<MemberDto>> searchMember(@RequestParam("keyword") String keyword) {
        try {
            getLoginMember();
            Map<String, Object> map = new HashMap<>();
            map.put("kwd", keyword); // ★ "keyword" -> "kwd"로 수정 (MyBatis Mapper의 kwd와 일치시킴)
            List<MemberDto> list = memberService.listFindMember(map);
            return ResponseEntity.ok(list);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 1:1 채팅방 생성 및 실시간 대화 요청
    @PostMapping("/mychat/room")
    @ResponseBody
    public ResponseEntity<?> getOrCreateRoom(@RequestBody Map<String, Object> body) {
        try {
            SessionInfo currentMember = getLoginMember();
            Long currentMemberId = currentMember.getMember_id();
            String currentMemberName = currentMember.getName();

            Object targetObj = body.get("targetMemberId");
            if (targetObj == null) {
                return ResponseEntity.badRequest().body("targetMemberId 전달 누락");
            }
            Long targetMemberId = Long.parseLong(targetObj.toString());

            // 1. 상대방 온라인 접속 여부 확인
            boolean isOnline = sessionRegistry.isOnline(targetMemberId);
            if (!isOnline) {
                return ResponseEntity.ok(Map.of(
                    "isOnline", false,
                    "message", "상대방이 현재 오프라인 상태입니다."
                ));
            }

            // 2. 채팅방 ID 생성 (실제 구현시 DB 조회/생성 로직으로 대체)
            Long roomId = currentMemberId < targetMemberId 
                ? currentMemberId * 10000 + targetMemberId 
                : targetMemberId * 10000 + currentMemberId;

            // 3. 온라인인 상대방에게 실시간 초대 메시지 발송 (/user/{targetMemberId}/queue/invite)
            Map<String, Object> invitePayload = new HashMap<>();
            invitePayload.put("type", "CHAT_INVITE");
            invitePayload.put("roomId", roomId);
            invitePayload.put("senderId", currentMemberId);
            invitePayload.put("senderName", currentMemberName);

            messagingTemplate.convertAndSendToUser(
                String.valueOf(targetMemberId),
                "/queue/invite",
                invitePayload
            );

            return ResponseEntity.ok(Map.of(
                "isOnline", true,
                "roomId", roomId
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        } catch (Exception e) {
            log.error("채팅방 생성 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage sendRoomMessage(@DestinationVariable("roomId") String roomId, ChatMessage message) {
        return message;
    }

    @GetMapping("/mychat/onlineUsers")
    @ResponseBody
    public ResponseEntity<?> getUserList() {
        return ResponseEntity.ok(sessionRegistry.getAllOnlineusers());
    }
}