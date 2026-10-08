package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityPostDto {

    private long postId;       // 게시글 번호
    private long authorId;     // 작성자 회원 번호
    private String authorName;
    private String title;
    private String content;
    private long viewCount;
    private String createdAt;
    
    private int commentCount;  // 공개 댓글·답글 수
    private int likeCount; 	   // 게시글 좋아요 수
}
