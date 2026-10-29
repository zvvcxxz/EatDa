package com.sp.app.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatMessage {
    private String type;        // TALK, LEAVE, ERROR 등
    private String roomId;
    private Long senderId;
    private String senderName;
    private String receiver;
    private String content;
    private String createdAtText;
}