package com.sp.app.chat;

import java.security.Principal;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.CustomUserDetails;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("ws-chat")
			.setAllowedOriginPatterns("*")
			.addInterceptors(new HttpSessionHandshakeInterceptor())
			.setHandshakeHandler(new DefaultHandshakeHandler() {
				@Override
				protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
					if (attributes != null) {
						// 1. "member" 세션 체크
						if (attributes.get("member") instanceof SessionInfo info && info.getMember_id() != null) {
							return () -> String.valueOf(info.getMember_id());
						}
						// 2. Spring Security Context 세션 체크
						if (attributes.get("SPRING_SECURITY_CONTEXT") instanceof SecurityContext context) {
							if (context.getAuthentication() != null && context.getAuthentication().getPrincipal() instanceof CustomUserDetails userDetails) {
								SessionInfo info = userDetails.getMember();
								if (info != null && info.getMember_id() != null) {
									return () -> String.valueOf(info.getMember_id());
								}
							}
						}
					}
					return super.determineUser(request, wsHandler, attributes);
				}
			})
			.withSockJS();
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}
}