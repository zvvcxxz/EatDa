package com.sp.app.security;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.sp.app.chat.ChatMessage;
import com.sp.app.chat.ChatRoomRegistry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final ChatRoomRegistry chatRoomRegistry;
    private final SimpMessagingTemplate messagingTemplate; //소켓 메세지 전송용

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Long memberId = userDetails.getMember().getMember_id();
            String memberName = userDetails.getMember().getName();
            
            // 로그아웃하는 유저가 참여 중인 모든 활성 대화방 조회
            List<ChatRoomRegistry.RoomInfo> activeRooms = chatRoomRegistry.getActiveRoomsForUser(memberId);
            
            for (ChatRoomRegistry.RoomInfo room : activeRooms) {
                String roomId = room.getRoomId();

                // 1. 상대방 대화창으로 실시간 퇴장 메시지 발송
                ChatMessage leaveMsg = ChatMessage.builder()
                        .type("LEAVE")
                        .roomId(roomId)
                        .senderId(memberId)
                        .senderName(memberName)
                        .content(memberName + "님이 로그아웃하셨습니다.")
                        .build();

                messagingTemplate.convertAndSend("/topic/chat/" + roomId, leaveMsg);

                // 2. 대화방 상태 CLOSED 처리
                chatRoomRegistry.closeRoom(roomId);
            }
        }
    }
}