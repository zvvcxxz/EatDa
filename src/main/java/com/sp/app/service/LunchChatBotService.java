package com.sp.app.service;

import java.nio.charset.StandardCharsets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class LunchChatBotService {
    private final ChatClient chatClient;
    private final Resource promptResource;

    public LunchChatBotService(
            ChatClient.Builder chatClientBuilder,
            @Value("classpath:prompts/lunch-prompt.txt") Resource promptResource) {

        this.chatClient = chatClientBuilder.build();
        this.promptResource = promptResource;
    }

    // 점심 추천 답변을 조금씩 받아오기
    public Flux<String> streamChat(String userMessage) {
        try {
            String systemPrompt = promptResource.getContentAsString(
                    StandardCharsets.UTF_8);

            Flux<String> result = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .stream()
                    .content();

            return result;
        } catch (Exception e) {
            return Flux.error(e);
        }
    }
}