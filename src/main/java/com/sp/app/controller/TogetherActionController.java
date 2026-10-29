package com.sp.app.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.TogetherActionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/together/actions")
public class TogetherActionController {
    private final TogetherActionService service;

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<?> error(ResponseStatusException error) {
        return org.springframework.http.ResponseEntity.status(error.getStatusCode())
                .body(Map.of("message", error.getReason() == null ? "요청을 처리하지 못했습니다." : error.getReason()));
    }

    private Long member(boolean required) {
        var info = LoginMemberUtil.getSessionInfo();
        Long id = info == null ? null : info.getMember_id();
        if (required && id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return id;
    }

    @GetMapping("/{type}/{id}")
    public Map<String, Object> state(@PathVariable("type") String type, @PathVariable("id") long id) {
        return service.state(type, id, member(false));
    }

    @PostMapping("/{type}/{id}/like")
    public Map<String, Object> like(@PathVariable("type") String type, @PathVariable("id") long id) {
        return service.like(type, id, member(true));
    }

    @PostMapping("/share/{id}/apply")
    public Map<String, Object> apply(@PathVariable("id") long id) {
        return service.apply(id, member(true));
    }

    @PostMapping("/share/{id}/applicants/{applicant}/{decision}")
    public Map<String, Object> decide(@PathVariable("id") long id, @PathVariable("applicant") long applicant,
            @PathVariable("decision") String decision) {
        return service.decide(id, applicant, member(true), decision);
    }
}
