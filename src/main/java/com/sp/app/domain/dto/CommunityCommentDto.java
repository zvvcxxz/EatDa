package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 댓글과 답글 정보를 담는 DTO
@Getter
@Setter
@NoArgsConstructor
public class CommunityCommentDto {

    private long commentId;       // 댓글 번호
    private long postId;          // 게시글 번호
    private long memberId;        // 작성자 회원 번호
    private Long parentCommentId; // 댓글은 null, 답글은 부모 댓글 번호

    private String memberName;
    private String content;
    private int status;          // 0: 공개, 1: 숨김, 2: 삭제
    private String createdAt;

    private boolean owner;       // 로그인 회원이 작성자인지 확인
}