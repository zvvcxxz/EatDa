package com.sp.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sp.app.domain.dto.MyFridgeDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MyFridgeService;
import com.sp.app.service.TogetherMeetService;
import com.sp.app.service.TogetherShareService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeCommunityController {
    private final TogetherMeetService meetService;
    private final TogetherShareService shareService;
    private final MyFridgeService fridgeService;

    public record PreviewItem(Long id, String title, String location, String date) {
    }

    // 메인 슬라이드 아래 : 최신 게시글 최대 5개 조회
    @GetMapping("/home/community-preview")
    public List<PreviewItem> preview(@RequestParam(defaultValue = "meet", name = "type") String type) {
        if (!"meet".equals(type) && !"share".equals(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시판 유형이 올바르지 않습니다.");
        }
        try {
            if ("meet".equals(type)) {
                return meetService.listMeet(0, 5).stream().map(post -> new PreviewItem(post.getMeet_id(),
                        post.getTitle(), post.getLocation_text(), post.getMeet_at())).toList();
            }
            return shareService.listShare(0, 5).stream().map(post -> new PreviewItem(post.getShare_id(),
                    post.getTitle(), post.getLocation_text(), post.getAvailable_until())).toList();
        } catch (Exception e) {
            log.error("메인 게시글 미리보기 조회 실패: {}", type, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게시글을 불러오지 못했습니다.");
        }
    }

    // 메인 : 로그인 상태와 내 냉장고 재료 반환
    @GetMapping("/home/items")
    public ResponseEntity<?> getFridgeItems() {
        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info != null && info.getMember_id() != null) {
            List<MyFridgeDto.ItemResponse> items = fridgeService.getMemberFridgeItems(info.getMember_id());
            return ResponseEntity.ok(Map.of("state", "login", "list", items));
        }

        return ResponseEntity.ok(Map.of("state", "logout"));
    }
}
