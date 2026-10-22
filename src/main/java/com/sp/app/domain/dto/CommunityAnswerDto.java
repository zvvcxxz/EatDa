package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityAnswerDto {

    private long commentId;   // 답변 번호
    private long postId;      // 질문 게시글 번호
    private long memberId;    // 답변 작성자 번호
    private String memberName;

    private String content;
    private int status;       // 0: 공개, 1: 숨김, 2: 삭제
    private String createdAt;

    private boolean owner;    // 본인이 작성한 답변인지 확인
}