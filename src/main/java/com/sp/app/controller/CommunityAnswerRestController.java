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
import com.sp.app.domain.dto.CommunityAnswerDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.CommunityAnswerService;
import com.sp.app.service.CommunityQuestionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/community/questions")
public class CommunityAnswerRestController {
	private final CommunityAnswerService service;
	private final CommunityQuestionService questionService;
	private final PaginateUtil paginateUtil;
	
	// 답변 목록 : JSON
	@GetMapping("/{postId}/answers")
	public ResponseEntity<?> listAnswer(
			@PathVariable(name = "postId") long postId,
			@RequestParam(name = "pageNo", defaultValue = "1") int current_page,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		SessionInfo info = LoginMemberUtil.getSessionInfo();
		
		if (info == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		try {
			if (questionService.findById(postId) == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			
			int size = 5;
			
			Map<String, Object> map = new HashMap<>();
			map.put("postId", postId);
			
			int totalCount = service.answerCount(map);
			int totalPage = paginateUtil.pageCount(totalCount, size);
			current_page = Math.max(1, Math.min(current_page, totalPage));
			
			int offset = (current_page - 1) * size;
			map.put("offset", offset);
			map.put("size", size);
			
			List<CommunityAnswerDto> listAnswer = service.listAnswer(map);
			
			for (CommunityAnswerDto dto : listAnswer) {
				dto.setOwner(dto.getMemberId() == info.getMember_id());
			}
			
			boolean admin = userDetails != null
					&& userDetails.getAuthorities().stream()
					.anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
			
			// AJAX로 답변 페이지를 변경
			String paging = paginateUtil.pagingMethod(current_page, totalPage, "loadContent");
			
			return ResponseEntity.ok(Map.of(
					"listAnswer", listAnswer,
					"pageNo", current_page,
					"totalCount", totalCount,
                    "totalPage", totalPage,
                    "paging", paging,
                    "admin", admin
			));
		} catch (Exception e) {
			log.info("listAnswer : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "답변을 불러오지 못했습니다."));
		}
	}
	
	// 답변 등록 : JSON
	@PostMapping("/{postId}/answers")
	public ResponseEntity<?> saveAnswer(
			@PathVariable(name = "postId") long postId,
			CommunityAnswerDto dto) {
		
		SessionInfo info = LoginMemberUtil.getSessionInfo();
		
		if (info == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		try {
			if (questionService.findById(postId) == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			
			if (dto.getContent() == null || dto.getContent().isBlank()) {
				return ResponseEntity.badRequest()
						.body(Map.of("message", "답변 내용을 입력하세요."));
			}
			
			dto.setPostId(postId);
			dto.setMemberId(info.getMember_id());
			dto.setContent(dto.getContent().trim());
			
			service.insertAnswer(dto);
			
			return ResponseEntity.ok().body(Map.of("success", true));
		} catch (Exception e) {
			log.info("saveAnswer : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "답변 등록에 실패했습니다."));
		}
	}
	
	// 본인 답변 수정 : JSON
	@PostMapping("/{postId}/answers/{commentId}/update")
	public ResponseEntity<?> updateAnswer(
			@PathVariable(name = "postId") long postId,
			@PathVariable(name = "commentId") long commentId,
			@RequestParam(name = "content") String content) {
		
		SessionInfo info = LoginMemberUtil.getSessionInfo();
		
		if (info == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		try {
			if (questionService.findById(postId) == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			
			CommunityAnswerDto dto = service.findById(commentId);
			
			if (dto == null || dto.getPostId() != postId
					|| dto.getStatus() != 0) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			
			// 다른 회원의 답변은 수정할 수 없음
			if (dto.getMemberId() != info.getMember_id()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			
			if (content.isBlank()) {
				return ResponseEntity.badRequest()
						.body(Map.of("message", "답변 내용을 입력하세요."));
			}
			
			dto.setContent(content.trim());
			
			if (service.updateAnswer(dto) == 0) {
				return ResponseEntity.badRequest()
						.body(Map.of("message", "수정할 답변이 없습니다."));
			}
			
			return ResponseEntity.ok().body(Map.of("success", true));
		} catch (Exception e) {
			log.info("updateAnswer : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "답변 수정에 실패했습니다."));
		}
	}
	
	// 본인 답변 삭제 : JSON
    @DeleteMapping("/{postId}/answers/{commentId}")
    public ResponseEntity<?> deleteAnswer(
            @PathVariable(name = "postId") long postId,
            @PathVariable(name = "commentId") long commentId) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            if (questionService.findById(postId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            CommunityAnswerDto dto = service.findById(commentId);

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

            if (service.deleteAnswer(map) == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "삭제할 답변이 없습니다."));
            }

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            log.info("deleteAnswer : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "답변 삭제에 실패했습니다."));
        }
    }

    // 관리자 답변 숨김 : JSON
    @PostMapping("/{postId}/answers/{commentId}/hide")
    public ResponseEntity<?> hideAnswer(
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
            if (questionService.findById(postId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            CommunityAnswerDto dto = service.findById(commentId);

            if (dto == null || dto.getPostId() != postId
                    || dto.getStatus() != 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("postId", postId);
            map.put("commentId", commentId);

            if (service.hideAnswer(map) == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "숨길 답변이 없습니다."));
            }

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            log.info("hideAnswer : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "답변 숨김에 실패했습니다."));
        }
    }
}