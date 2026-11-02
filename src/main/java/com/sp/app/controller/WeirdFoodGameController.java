package com.sp.app.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.service.WeirdFoodGameService;
import com.sp.app.service.WeirdFoodGameService.JudgeUnavailableException;
import com.sp.app.service.WeirdFoodImageService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home/weird-food")
public class WeirdFoodGameController {
    public record Combination(String prompt) {}
    public record ImageRequest(String ticket) {}
    public record Reply(String name, int score, String grade, String flavor,
            String comment, List<String> ingredients, String imageTicket) {}
    private final WeirdFoodGameService service;
    private final WeirdFoodImageService images;
    private static final String BUSY = "weirdFood.busy";
    private static final String NEXT = "weirdFood.next";
    private static final String IMAGE = "weirdFood.image";

    // 세션에는 마지막 결과 한 개만 보관하고 10분 뒤 만료
    private static final class ImageJob {
        final String ticket = UUID.randomUUID().toString();
        final long expires = System.currentTimeMillis() + 600_000;
        final WeirdFoodGameService.Result result;
        boolean busy;
        int attempts;
        WeirdFoodImageService.Picture picture;
        ImageJob(WeirdFoodGameService.Result result) { this.result = result; }
    }

    // 입력한 문장을 그대로 AI에 전달하여 맥락과 재료를 파악
    @PostMapping("/judge")
    public ResponseEntity<?> judge(@RequestBody Combination body, HttpSession session) {
        final String prompt;
        try {
            prompt = service.validatePrompt(body == null ? null : body.prompt());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        synchronized (session) {
            long now = System.currentTimeMillis();
            Long next = (Long) session.getAttribute(NEXT);
            if (Boolean.TRUE.equals(session.getAttribute(BUSY)) || (next != null && now < next)) {
                int seconds = next == null ? 1 : (int) Math.max(1, (next - now + 999) / 1000);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", String.valueOf(seconds))
                        .body(Map.of("message", "잠시 쉬었다 다시 심사해주세요.", "retryAfter", seconds));
            }
            session.setAttribute(BUSY, true);
            session.setAttribute(NEXT, now + 15_000);
        }
        try {
            var result = service.judge(prompt);
            var job = new ImageJob(result);
            synchronized (session) { session.setAttribute(IMAGE, job); }
            return ResponseEntity.ok(new Reply(result.name(), result.score(), result.grade(),
                    result.flavor(), result.comment(), result.ingredients(), job.ticket));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (JudgeUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", e.getMessage()));
        } finally {
            synchronized (session) { session.removeAttribute(BUSY); }
        }
    }

    // 이미 심사한 결과만 이미지로 생성. 클라이언트가 임의 프롬프트를 넣지 못하게 제한
    @PostMapping("/image")
    public ResponseEntity<?> image(@RequestBody ImageRequest body, HttpSession session) {
        final ImageJob job;
        synchronized (session) {
            job = (ImageJob) session.getAttribute(IMAGE);
            if (job == null || body == null || !job.ticket.equals(body.ticket())
                    || System.currentTimeMillis() > job.expires) {
                if (job != null && System.currentTimeMillis() > job.expires) session.removeAttribute(IMAGE);
                return ResponseEntity.badRequest().body(Map.of("message", "결과가 만료됐어요. 다시 조합해주세요."));
            }
            if (job.picture != null) return imageResponse(job.picture);
            if (job.busy) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 생성 중이에요."));
            if (job.attempts >= 2) return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "이 결과의 이미지 재시도 횟수를 초과했어요. 새 조합으로 도전해주세요."));
            job.busy = true;
            job.attempts++;
        }
        try {
            var picture = images.generate(job.result);
            synchronized (session) { job.picture = picture; }
            return imageResponse(picture);
        } catch (JudgeUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", e.getMessage()));
        } finally {
            synchronized (session) { job.busy = false; }
        }
    }

    private ResponseEntity<byte[]> imageResponse(WeirdFoodImageService.Picture picture) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(picture.mimeType()))
                .header("Cache-Control", "no-store").header("X-Content-Type-Options", "nosniff")
                .body(picture.bytes());
    }
}
