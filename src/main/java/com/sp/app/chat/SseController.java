package com.sp.app.chat;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseNotificationService sseNotificationService;

    @GetMapping(value = "/api/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe() {
        SessionInfo session = LoginMemberUtil.getSessionInfo();
        
        // 세션이 없으면 401 Unauthorized 반환 (Thymeleaf 뷰 렌더링 방지)
        if (session == null || session.getMember_id() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SseEmitter emitter = sseNotificationService.subscribe(session.getMember_id());
        return ResponseEntity.ok(emitter);
    }
}