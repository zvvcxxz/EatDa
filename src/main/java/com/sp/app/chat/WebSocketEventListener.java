package com.sp.app.chat;

import java.util.HashMap;
import java.util.Map;

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
	
	private final UserSessionRegistry sessionRegistry;
	private final SimpMessagingTemplate messagingTemplate;
	
	@EventListener
	public void handleConnect(SessionConnectEvent event) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
		Map<String, Object> sessionAttributes = sha.getSessionAttributes();
		
		SessionInfo info = extractSessionInfo(sessionAttributes, sha);
		
		if (info != null && info.getMember_id() != null) {
			sessionRegistry.add(info.getMember_id(), info.getName());
			log.info("[WebSocket] 온라인 등록 완료 - 회원ID: {}, 이름: {}", info.getMember_id(), info.getName());
			
			Map<String, Object> payload = new HashMap<>();
			payload.put("uid", info.getMember_id());
			payload.put("name", info.getName());
			
			messagingTemplate.convertAndSend("/topic/online", (Object)payload);		
		} else {
			log.warn("[WebSocket] 연결되었으나 SessionInfo를 찾을 수 없음");
		}
	}
	
	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
		Map<String, Object> sessionAttributes = sha.getSessionAttributes();
		
		SessionInfo info = extractSessionInfo(sessionAttributes, sha);
		
		if (info != null && info.getMember_id() != null) {
			sessionRegistry.remove(info.getMember_id());
			log.info("[WebSocket] 오프라인 처리 완료 - 회원ID: {}", info.getMember_id());
			
			Map<String, Object> payload = Map.of(
					"uid", info.getMember_id(),
					"name", info.getName());
			
			messagingTemplate.convertAndSend("/topic/offline", (Object)payload);
		}
	}

	private SessionInfo extractSessionInfo(Map<String, Object> sessionAttributes, StompHeaderAccessor sha) {
		if (sessionAttributes != null) {
			// 1. 세션에 "member" 키로 직접 들어있는 경우
			if (sessionAttributes.get("member") instanceof SessionInfo info) {
				return info;
			}

			// 2. Spring Security Context에서 추출 (Principal = CustomUserDetails)
			if (sessionAttributes.get("SPRING_SECURITY_CONTEXT") instanceof SecurityContext context) {
				if (context.getAuthentication() != null && context.getAuthentication().getPrincipal() instanceof CustomUserDetails userDetails) {
					return userDetails.getMember();
				}
			}
		}

		// 3. StompHeaderAccessor User(Authentication)에서 추출
		if (sha.getUser() instanceof org.springframework.security.core.Authentication auth) {
			if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
				return userDetails.getMember();
			}
		}

		return null;
	}
}