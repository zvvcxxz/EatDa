package com.sp.app.chat;

import lombok.Data;

@Data
public class ChatMessage {
	private String type;// TALK, LEAVE 등 메시지 타입
    private String roomId;
    private Long senderId;
    private String senderName;
    private String receiver;
    private String content;
    private String createdAtText;
}
