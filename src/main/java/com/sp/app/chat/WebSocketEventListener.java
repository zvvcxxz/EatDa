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

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatRoomRegistry chatRoomRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final SseNotificationService sseNotificationService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<Long, ScheduledFuture<?>> pendingDisconnectTasks = new ConcurrentHashMap<>();

    //서버 종료(Tomcat 종료) 시 스레드 풀을 즉시 종료하여 STS 셧다운 지연 방지
    @PreDestroy
    public void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        SessionInfo info = extractSessionInfo(sha);

        if (info != null && info.getMember_id() != null) {
            ScheduledFuture<?> task = pendingDisconnectTasks.remove(info.getMember_id());
            if (task != null) {
                task.cancel(false);
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        SessionInfo info = extractSessionInfo(sha);

        if (info != null && info.getMember_id() != null) {
            Long memberId = info.getMember_id();
            String memberName = info.getName();

         // 페이지 이동 시 새 페이지에서 global-sse.js 연결이 안정화되도록 5초 유예
            ScheduledFuture<?> task = scheduler.schedule(() -> {
                pendingDisconnectTasks.remove(memberId);

                // 웹소켓은 끊겼지만 다른 페이지에서 SSE 연결이 유지 중인지 확인
                if (sseNotificationService.isConnected(memberId)) {
                    log.info("[WebSocket] 페이지 이동 감지 - 회원ID: {} (SSE 연결 유지 중이므로 대화방을 닫지 않습니다.)", memberId);
                    return;
                }

                List<ChatRoomRegistry.RoomInfo> activeRooms = chatRoomRegistry.getActiveRoomsForUser(memberId);
                for (ChatRoomRegistry.RoomInfo room : activeRooms) {
                    String roomId = room.getRoomId();

                    ChatMessage leaveMsg = ChatMessage.builder()
                            .type("LEAVE")
                            .roomId(roomId)
                            .senderId(memberId)
                            .senderName(memberName)
                            .content(memberName + "님이 접속을 종료하였습니다.")
                            .build();

                    messagingTemplate.convertAndSend("/topic/chat/" + roomId, leaveMsg);
                    chatRoomRegistry.closeRoom(roomId);
                    log.info("[WebSocket] 완전한 접속 단절 감지 - 방 번호: {}, 회원: {}", roomId, memberName);
                }
            }, 5000, TimeUnit.MILLISECONDS); // ★ 3초 -> 5초로 확장

            pendingDisconnectTasks.put(memberId, task);
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