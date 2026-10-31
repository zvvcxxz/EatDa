package com.sp.app.chat;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SseNotificationService {

    private static final Long DEFAULT_TIMEOUT = 60 * 1000L * 30; // 30분
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long memberId) {
        closeEmitter(memberId);

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 구 연결의 콜백이 신규 emitter를 삭제하지 못하도록 (memberId, emitter) 쌍으로 삭제
        emitter.onCompletion(() -> emitters.remove(memberId, emitter));
        emitter.onTimeout(() -> emitters.remove(memberId, emitter));
        emitter.onError((e) -> emitters.remove(memberId, emitter));

        emitters.put(memberId, emitter);
        log.info("[SSE] 구독 등록 완료 - 회원ID: {}", memberId);

        // INIT 메시지는 Spring MVC 핸들러가 응답을 생성한 직후 비동기로 안전하게 전송
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(150);
                sendToClient(memberId, "INIT", Map.of("message", "SSE Connected"));
            } catch (Exception ignored) {}
        });

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
                // 해당 emitter 인스턴스만 안전하게 제거
                emitters.remove(memberId, emitter);
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
                emitters.remove(memberId, emitter);
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