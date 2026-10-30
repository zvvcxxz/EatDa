package com.sp.app.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;

@Service
public class SseNotificationService {

    private static final Long DEFAULT_TIMEOUT = 60 * 1000L * 30; // 30분
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long memberId) {
        closeEmitter(memberId);

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.put(memberId, emitter);

        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));
        emitter.onError((e) -> emitters.remove(memberId));

        sendToClient(memberId, "INIT", Map.of("message", "SSE Connected"));

        return emitter;
    }

    public void sendToClient(Long memberId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(memberId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {}
            }
        }
    }

    // 15초마다 Ping을 전송하여 커넥션 유휴 타임아웃 방지
    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        emitters.forEach((memberId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("PING")
                        .data("keep-alive", MediaType.TEXT_PLAIN));
            } catch (Exception e) {
                emitters.remove(memberId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {}
            }
        });
    }

    public void closeEmitter(Long memberId) {
        SseEmitter emitter = emitters.remove(memberId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception ignored) {}
        }
    }

    //서버 종료 시 모든 SSE 연결 해제
    @PreDestroy
    public void clearAllEmitters() {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.complete();
            } catch (Exception ignored) {}
        });
        emitters.clear();
    }

    public boolean isConnected(Long memberId) {
        return emitters.containsKey(memberId);
    }
}