package com.sp.app.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.sp.app.admin.service.ChatbotService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class ChatBotRestController {
	private final ChatbotService chatBotService;
	
    @GetMapping("/api/question")
	public Flux<String> handleChat(@RequestParam("question") String question) throws Exception {
    	return chatBotService.generateAnswer(question);
	}
    
    @GetMapping("/aichatbot/chatbot")
    public ModelAndView chatView() {
    	return new ModelAndView("/aichatbot/chatbot");
    }
}
