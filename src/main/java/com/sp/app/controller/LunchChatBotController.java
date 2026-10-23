package com.sp.app.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.app.service.LunchChatBotService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ai/aichatbot")
public class LunchChatBotController {
    private final LunchChatBotService service;

    // 모달 안에서 보여줄 챗봇 화면
    @GetMapping({"", "/"})
    public String main() {
        return "aichatbot/lunchChat";
    }

    // 답변을 조금씩 전송
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamChat(
            @RequestParam("message") String message) {

        return service.streamChat(message);
    }
}