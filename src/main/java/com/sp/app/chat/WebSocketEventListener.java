package com.sp.app.chat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatRoomRegistry chatRoomRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    // 3초 유예 시간용 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<Long, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();

    // 페이지 이동 후 재접속 시 예약 취소
    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        SessionInfo info = extractSessionInfo(sha);

        if (info != null && info.getMember_id() != null) {
            ScheduledFuture<?> task = pendingTasks.remove(info.getMember_id());
            if (task != null) {
                task.cancel(false); // 페이지 이동이었으므로 퇴장 알림 취소
            }
        }
    }

    // 소켓 끊김 감지 (브라우저 종료 또는 페이지 이동)
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        SessionInfo info = extractSessionInfo(sha);

        if (info != null && info.getMember_id() != null) {
            Long memberId = info.getMember_id();
            String memberName = info.getName();

            // 2.5초 후에도 다시 연결되지 않으면 실제 브라우저 종료로 간주
            ScheduledFuture<?> task = scheduler.schedule(() -> {
                pendingTasks.remove(memberId);

                List<ChatRoomRegistry.RoomInfo> activeRooms = chatRoomRegistry.getActiveRoomsForUser(memberId);
                for (ChatRoomRegistry.RoomInfo room : activeRooms) {
                    String roomId = room.getRoomId();

                    // 대화방으로 종료 알림 전송
                    ChatMessage leaveMsg = ChatMessage.builder()
                            .type("LEAVE")
                            .roomId(roomId)
                            .senderId(memberId)
                            .senderName(memberName)
                            .content(memberName + "님이 접속을 종료하셨습니다.")
                            .build();

                    messagingTemplate.convertAndSend("/topic/chat/" + roomId, leaveMsg);
                    chatRoomRegistry.closeRoom(roomId);
                }
            }, 2500, TimeUnit.MILLISECONDS);

            pendingTasks.put(memberId, task);
        }
    }

    private SessionInfo extractSessionInfo(StompHeaderAccessor sha) {
        if (sha.getSessionAttributes() != null) {
            if (sha.getSessionAttributes().get("SPRING_SECURITY_CONTEXT") instanceof SecurityContext context) {
                if (context.getAuthentication() != null && context.getAuthentication().getPrincipal() instanceof CustomUserDetails userDetails) {
                    return userDetails.getMember();
                }
            }
        }
        if (sha.getUser() instanceof org.springframework.security.core.Authentication auth) {
            if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                return userDetails.getMember();
            }
        }
        return null;
    }
}