package com.sp.app.security;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.sp.app.chat.ChatMessage;
import com.sp.app.chat.ChatRoomRegistry;
import com.sp.app.chat.SseNotificationService; 

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final ChatRoomRegistry chatRoomRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final SseNotificationService sseNotificationService; 

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Long memberId = userDetails.getMember().getMember_id();
            String memberName = userDetails.getMember().getName();
            
            // 1. 활성화된 대화방 종료 알림 및 CLOSED 처리
            List<ChatRoomRegistry.RoomInfo> activeRooms = chatRoomRegistry.getActiveRoomsForUser(memberId);
            for (ChatRoomRegistry.RoomInfo room : activeRooms) {
                String roomId = room.getRoomId();

                ChatMessage leaveMsg = ChatMessage.builder()
                        .type("LEAVE")
                        .roomId(roomId)
                        .senderId(memberId)
                        .senderName(memberName)
                        .content(memberName + "님이 로그아웃하셨습니다.")
                        .build();

                messagingTemplate.convertAndSend("/topic/chat/" + roomId, leaveMsg);
                chatRoomRegistry.closeRoom(roomId);
            }

            // 2. 로그아웃 유저의 SSE 연결 제거 (상대방이 오프라인 감지 가능하게 만듦)
            sseNotificationService.closeEmitter(memberId);
        }
    }
}