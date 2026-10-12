package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityCommentDto {
    private long commentId;
    private long postId;
    private long memberId;
    private String memberName;

    private Long parentCommentId; // 댓글은 null, 답글은 부모 댓글 번호
    private String content;
    private int status;           // 0: 공개, 1: 숨김, 2: 삭제
    private String createdAt;

    private int answerCount;      // 답글 개수
    private boolean owner;        // 본인이 작성한 댓글인지 확인
}