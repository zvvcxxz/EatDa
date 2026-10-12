package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.common.PaginateUtil;
import com.sp.app.domain.dto.CommunityCommentDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.CommunityCommentService;
import com.sp.app.service.CommunityPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/community/free")
public class CommunityCommentRestController {
    private final CommunityCommentService service;
    private final CommunityPostService postService;
    private final PaginateUtil paginateUtil;

    // 댓글 목록 : JSON
    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> listComment(
            @PathVariable(name = "postId") long postId,
            @RequestParam(name = "pageNo", defaultValue = "1") int current_page,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            if (postService.findById(postId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            int size = 5;

            Map<String, Object> map = new HashMap<>();
            map.put("postId", postId);

            int totalCount = service.commentCount(map);
            int totalPage = paginateUtil.pageCount(totalCount, size);
            current_page = Math.max(1, Math.min(current_page, totalPage));

            int offset = (current_page - 1) * size;
            map.put("offset", offset);
            map.put("size", size);

            List<CommunityCommentDto> listComment = service.listComment(map);

            for (CommunityCommentDto dto : listComment) {
                dto.setOwner(dto.getMemberId() == info.getMember_id());
            }

            boolean admin = userDetails != null
                    && userDetails.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

            // AJAX로 댓글 페이지를 변경
            String paging = paginateUtil.pagingMethod(
                    current_page, totalPage, "loadContent");

            return ResponseEntity.ok(Map.of(
                    "listComment", listComment,
                    "pageNo", current_page,
                    "totalCount", totalCount,
                    "totalPage", totalPage,
                    "paging", paging,
                    "admin", admin
            ));
        } catch (Exception e) {
            log.info("listComment : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "댓글을 불러오지 못했습니다."));
        }
    }

    // 댓글 및 답글 등록 : JSON
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> saveComment(
            @PathVariable(name = "postId") long postId,
            CommunityCommentDto dto) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            if (postService.findById(postId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (dto.getContent() == null || dto.getContent().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "댓글 내용을 입력하세요."));
            }

            // 답글은 같은 게시글의 공개 댓글에만 등록
            if (dto.getParentCommentId() != null) {
                CommunityCommentDto parent =
                        service.findById(dto.getParentCommentId());

                if (parent == null || parent.getPostId() != postId
                        || parent.getStatus() != 0
                        || parent.getParentCommentId() != null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "답글을 등록할 댓글이 없습니다."));
                }
            }

            dto.setPostId(postId);
            dto.setMemberId(info.getMember_id());
            dto.setContent(dto.getContent().trim());

            service.insertComment(dto);

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            log.info("saveComment : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "댓글 등록에 실패했습니다."));
        }
    }

    // 본인 댓글 및 답글 수정 : JSON
    @PostMapping("/{postId}/comments/{commentId}/update")
    public ResponseEntity<?> updateComment(
            @PathVariable(name = "postId") long postId,
            @PathVariable(name = "commentId") long commentId,
            @RequestParam(name = "content") String content) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            if (postService.findById(postId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            CommunityCommentDto dto = service.findById(commentId);

            if (dto == null || dto.getPostId() != postId
                    || dto.getStatus() != 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 다른 회원의 댓글은 수정할 수 없음
            if (dto.getMemberId() != info.getMember_id()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (content.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "댓글 내용을 입력하세요."));
            }

            dto.setContent(content.trim());

            if (service.updateComment(dto) == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "수정할 댓글이 없습니다."));
            }

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            log.info("updateComment : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "댓글 수정에 실패했습니다."));
        }
    }

    // 본인 댓글 및 답글 삭제 : JSON
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable(name = "postId") long postId,
            @PathVariable(name = "commentId") long commentId) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            if (postService.findById(postId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            CommunityCommentDto dto = service.findById(commentId);

            if (dto == null || dto.getPostId() != postId
                    || dto.getStatus() != 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (dto.getMemberId() != info.getMember_id()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("postId", postId);
            map.put("commentId", commentId);
            map.put("memberId", info.getMember_id());

            if (service.deleteComment(map) == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "삭제할 댓글이 없습니다."));
            }

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            log.info("deleteComment : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "댓글 삭제에 실패했습니다."));
        }
    }

    // 선택한 댓글의 답글 목록 : JSON
    @GetMapping("/{postId}/comments/{commentId}/answers")
    public ResponseEntity<?> listCommentAnswer(
            @PathVariable(name = "postId") long postId,
            @PathVariable(name = "commentId") long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            if (postService.findById(postId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            CommunityCommentDto parent = service.findById(commentId);

            if (parent == null || parent.getPostId() != postId
                    || parent.getStatus() != 0
                    || parent.getParentCommentId() != null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("postId", postId);
            map.put("parentCommentId", commentId);

            int answerCount = service.commentAnswerCount(map);
            List<CommunityCommentDto> listAnswers =
                    service.listCommentAnswer(map);

            for (CommunityCommentDto dto : listAnswers) {
                dto.setOwner(dto.getMemberId() == info.getMember_id());
            }

            boolean admin = userDetails != null
                    && userDetails.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

            return ResponseEntity.ok(Map.of(
                    "answerCount", answerCount,
                    "listAnswers", listAnswers,
                    "admin", admin
            ));
        } catch (Exception e) {
            log.info("listCommentAnswer : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "답글을 불러오지 못했습니다."));
        }
    }

    // 관리자 댓글 및 답글 숨김 : JSON
    @PostMapping("/{postId}/comments/{commentId}/hide")
    public ResponseEntity<?> hideComment(
            @PathVariable(name = "postId") long postId,
            @PathVariable(name = "commentId") long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null || userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        if (!admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            if (postService.findById(postId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            CommunityCommentDto dto = service.findById(commentId);

            if (dto == null || dto.getPostId() != postId
                    || dto.getStatus() != 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("postId", postId);
            map.put("commentId", commentId);

            if (service.hideComment(map) == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "숨길 댓글이 없습니다."));
            }

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            log.info("hideComment : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "댓글 숨김에 실패했습니다."));
        }
    }
}