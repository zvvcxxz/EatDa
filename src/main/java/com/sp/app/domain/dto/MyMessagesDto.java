package com.sp.app.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MyMessagesDto {

    //쪽지 전송 요청
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendRequest {
        private List<Long> recipientIds; // 다중 수신자 ID 리스트
        private String title;
        private String content;
    }

    // 쪽지 목록 및 상세 응답 DTO
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long senderId;
        private String senderNickname;
        private Long recipientId;
        private String recipientNickname;
        private String counterpartNickname; // 목록 표기용 (상대방 닉네임)
        private String title;
        private String preview;
        private String content;
        private boolean read;
        private String readAtText;
        private String createdAtText;
    }

    // 회원 검색 결과 DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberSearchResult {
        private Long id;
        private String name;
    }
}