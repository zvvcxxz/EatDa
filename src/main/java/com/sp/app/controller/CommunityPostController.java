package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.sp.app.common.PaginateUtil;
import com.sp.app.domain.dto.CommunityPostDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.CommunityPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/community/free")
public class CommunityPostController {

    private final CommunityPostService service;
    private final PaginateUtil paginateUtil;

    // 목록과 검색
    @GetMapping({"", "/"})
    public String list(
            @RequestParam(name = "page", defaultValue = "1") int current_page,
            @RequestParam(name = "schType", defaultValue = "all") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (userDetails == null) {
            return "redirect:/member/login";
        }

        if (!schType.equals("all") && !schType.equals("title")
                && !schType.equals("content")) {
            schType = "all";
        }

        kwd = kwd.trim();
        int size = 6; // 카드 목록: 3열 2행

        Map<String, Object> map = new HashMap<>();
        map.put("schType", schType);
        map.put("kwd", kwd);

        int dataCount = service.dataCount(map);
        int totalPage = paginateUtil.pageCount(dataCount, size);

        if (totalPage == 0) {
            totalPage = 1;
        }

        current_page = Math.max(1, Math.min(current_page, totalPage));

        // 현재 페이지 앞의 글 수만큼 건너뛰기
        int offset = (current_page - 1) * size;
        map.put("offset", offset);
        map.put("size", size);

        List<CommunityPostDto> list = service.listPost(map);

        model.addAttribute("posts", list);
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("page", current_page);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        model.addAttribute("pageStart", Math.max(1, current_page - 2));
        model.addAttribute("pageEnd", Math.min(totalPage, current_page + 2));

        model.addAttribute("pageTitle", "자유 이야기");
        model.addAttribute("activeMenu", "free");
        model.addAttribute("communityMember", userDetails.getMember());

        return "community/communityPost-list";
    }

    // 상세와 이전글·다음글 조회
    @GetMapping("/{postId}")
    public String article(
            @PathVariable("postId") long postId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "schType", defaultValue = "all") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            Model model) throws Exception {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        // 공개된 글만 조회수 증가
        if (service.updateHitCount(postId) == 0) {
            return "redirect:/community/free";
        }

        CommunityPostDto dto = service.findById(postId);

        if (dto == null) {
            return "redirect:/community/free";
        }

        if (!schType.equals("all") && !schType.equals("title")
                && !schType.equals("content")) {
            schType = "all";
        }

        kwd = kwd.trim();

        // 같은 검색 조건 안에서 이전글·다음글 찾기
        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("schType", schType);
        map.put("kwd", kwd);

        model.addAttribute("prevPost", service.findByPrev(map));
        model.addAttribute("nextPost", service.findByNext(map));

        map.put("memberId", info.getMember_id());
        model.addAttribute("liked", service.hasPostLike(map) > 0);

        model.addAttribute("post", dto);
        model.addAttribute("page", Math.max(1, page));
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);

        // 본인 글에만 수정·삭제 버튼 표시
        model.addAttribute("owner",
                dto.getAuthorId() == info.getMember_id());

        model.addAttribute("pageTitle", "자유 이야기 상세");
        model.addAttribute("activeMenu", "free");
        model.addAttribute("communityMember", info);

        return "community/communityPost-article";
    }

    // 등록 화면
    @GetMapping("/write")
    public String writeForm(Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        model.addAttribute("post", new CommunityPostDto());
        model.addAttribute("mode", "write");
        model.addAttribute("pageTitle", "자유 이야기 등록");
        model.addAttribute("activeMenu", "free");

        return "community/communityPost-write";
    }

    // 등록 처리
    @PostMapping("/write")
    public String writeSubmit(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityPostDto dto = new CommunityPostDto();
        dto.setTitle(title.trim());
        dto.setContent(content);
        dto.setAuthorId(info.getMember_id());

        model.addAttribute("post", dto);
        model.addAttribute("mode", "write");
        model.addAttribute("pageTitle", "자유 이야기 등록");
        model.addAttribute("activeMenu", "free");

        if (dto.getTitle().isBlank() || dto.getTitle().length() > 200
                || content.isBlank()) {
            model.addAttribute("error",
                    "제목은 1~200자로 입력하고 내용을 작성해 주세요.");
            return "community/communityPost-write";
        }

        try {
            service.insertPost(dto);
        } catch (Exception e) {
            log.info("writeSubmit : ", e);
            model.addAttribute("error", "게시글 등록에 실패했습니다.");
            return "community/communityPost-write";
        }

        return "redirect:/community/free";
    }

    // 수정 화면
    @GetMapping("/{postId}/update")
    public String updateForm(
            @PathVariable("postId") long postId,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityPostDto dto = service.findById(postId);

        if (dto == null) {
            return "redirect:/community/free";
        }

        if (dto.getAuthorId() != info.getMember_id()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("post", dto);
        model.addAttribute("mode", "update");
        model.addAttribute("pageTitle", "자유 이야기 수정");
        model.addAttribute("activeMenu", "free");

        return "community/communityPost-write";
    }

    // 수정 처리
    @PostMapping("/{postId}/update")
    public String updateSubmit(
            @PathVariable("postId") long postId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityPostDto dto = service.findById(postId);

        if (dto == null) {
            return "redirect:/community/free";
        }

        // 주소를 직접 입력해도 작성자 확인
        if (dto.getAuthorId() != info.getMember_id()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        dto.setTitle(title.trim());
        dto.setContent(content);
        dto.setAuthorId(info.getMember_id());

        model.addAttribute("post", dto);
        model.addAttribute("mode", "update");
        model.addAttribute("pageTitle", "자유 이야기 수정");
        model.addAttribute("activeMenu", "free");

        if (dto.getTitle().isBlank() || dto.getTitle().length() > 200
                || content.isBlank()) {
            model.addAttribute("error",
                    "제목은 1~200자로 입력하고 내용을 작성해 주세요.");
            return "community/communityPost-write";
        }

        try {
            if (service.updatePost(dto) == 0) {
                return "redirect:/community/free";
            }
        } catch (Exception e) {
            log.info("updateSubmit : ", e);
            model.addAttribute("error", "게시글 수정에 실패했습니다.");
            return "community/communityPost-write";
        }

        return "redirect:/community/free/" + postId;
    }

    // 본인 게시글 삭제
    @PostMapping("/{postId}/delete")
    public String delete(
            @PathVariable("postId") long postId) throws Exception {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityPostDto dto = service.findById(postId);

        if (dto == null) {
            return "redirect:/community/free";
        }

        if (dto.getAuthorId() != info.getMember_id()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("authorId", info.getMember_id());

        service.deletePost(map);

        return "redirect:/community/free";
    }

    // 관리자 게시글 숨김
    @PostMapping("/{postId}/hide")
    public String hide(
            @PathVariable("postId") long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails)
            throws Exception {

        if (userDetails == null) {
            return "redirect:/member/login";
        }

        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth ->
                        auth.getAuthority().equals("ROLE_ADMIN"));

        if (!admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        service.hidePost(postId);

        return "redirect:/community/free";
    }

    // 좋아요 등록 : AJAX-JSON
    @PostMapping("/like/{postId}")
    public ResponseEntity<?> insertPostLike(
            @PathVariable("postId") long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        if (service.findById(postId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "조회할 글이 없습니다."));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("memberId", userDetails.getMember().getMember_id());

        try {
            service.insertPostLike(map);
        } catch (DuplicateKeyException e) {
            // 중복 요청이면 기존 좋아요 유지
        } catch (Exception e) {
            log.info("insertPostLike : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "좋아요 처리에 실패했습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "likeCount", service.postLikeCount(postId),
                "liked", service.hasPostLike(map) > 0));
    }

    // 좋아요 취소 : AJAX-JSON
    @DeleteMapping("/like/{postId}")
    public ResponseEntity<?> deletePostLike(
            @PathVariable("postId") long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        if (service.findById(postId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "조회할 글이 없습니다."));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("memberId", userDetails.getMember().getMember_id());

        try {
            service.deletePostLike(map);

            return ResponseEntity.ok(Map.of(
                    "likeCount", service.postLikeCount(postId),
                    "liked", service.hasPostLike(map) > 0));

        } catch (Exception e) {
            log.info("deletePostLike : ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "좋아요 취소에 실패했습니다."));
        }
    }
}