package com.sp.app.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
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

    private final ChatRoomRegistry chatRoomRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
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
            map.put("kwd", keyword);
            List<MemberDto> list = memberService.listFindMember(map);
            return ResponseEntity.ok(list);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

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

            String roomId = chatRoomRegistry.generateRoomId(currentMemberId, targetMemberId);

            if (chatRoomRegistry.isRoomActive(roomId)) {
                return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "message", "이미 대화하고 있는 대화방이 존재합니다."
                ));
            }

            boolean isTargetConnected = simpUserRegistry.getUser(String.valueOf(targetMemberId)) != null;
            if (!isTargetConnected) {
                return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "isOnline", false,
                    "message", "상대방이 현재 로그인되어 있지 않거나 접속 중이 아닙니다."
                ));
            }

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
                "exists", false,
                "isOnline", true,
                "status", "PENDING",
                "message", "상대방에게 대화 요청을 보냈습니다. 응답을 기다리는 중입니다."
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        } catch (Exception e) {
            log.error("채팅방 생성 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mychat/respondInvite")
    @ResponseBody
    public ResponseEntity<?> respondInvite(@RequestBody Map<String, Object> body) {
        try {
            SessionInfo currentMember = getLoginMember();
            Long currentMemberId = currentMember.getMember_id();

            String roomId = body.get("roomId").toString();
            Long targetUserId = Long.parseLong(body.get("targetUserId").toString());
            boolean accept = Boolean.parseBoolean(body.get("accept").toString());

            Map<String, Object> responsePayload = new HashMap<>();

            if (accept) {
                chatRoomRegistry.activateRoom(roomId, currentMemberId, targetUserId);
                
                responsePayload.put("type", "INVITE_ACCEPTED");
                responsePayload.put("roomId", roomId);
                responsePayload.put("message", currentMember.getName() + "님이 대화 요청을 수락했습니다.");
            } else {
                responsePayload.put("type", "INVITE_REJECTED");
                responsePayload.put("message", "상대방이 채팅 요청을 거절했습니다.");
            }

            messagingTemplate.convertAndSendToUser(
                String.valueOf(targetUserId),
                "/queue/inviteResponse",
                responsePayload
            );

            return ResponseEntity.ok(Map.of("success", true, "accept", accept, "roomId", roomId));
        } catch (Exception e) {
            log.error("초대 응답 처리 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendRoomMessage(@DestinationVariable("roomId") String roomId, ChatMessage message) {
        if ("LEAVE".equals(message.getType())) {
            chatRoomRegistry.closeRoom(roomId);
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
            return;
        }

        if (!chatRoomRegistry.isRoomActive(roomId)) {
            ChatMessage errorMsg = ChatMessage.builder()
                .type("ERROR")
                .content("메세지를 보낼 수 없습니다. 나간 후 다시 초대해주세요.")
                .build();

            messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getSenderId()),
                "/queue/private",
                errorMsg
            );
            return;
        }

        chatRoomRegistry.saveMessage(roomId, message);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);

        if (message.getReceiver() != null && !message.getReceiver().isEmpty()) {
            messagingTemplate.convertAndSendToUser(
                message.getReceiver(),
                "/queue/private",
                message
            );
        }
    }
}