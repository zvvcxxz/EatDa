package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

@Controller
@RequiredArgsConstructor
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

        // 정해진 검색 항목만 사용
        if (!schType.equals("all")
                && !schType.equals("title")
                && !schType.equals("content")) {
            schType = "all";
        }

        kwd = kwd.trim();
        int size = 10;

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

        // 화면에 표시할 페이지 번호 범위
        model.addAttribute("pageStart", Math.max(1, current_page - 2));
        model.addAttribute("pageEnd", Math.min(totalPage, current_page + 2));

        model.addAttribute("pageTitle", "자유 이야기");
        model.addAttribute("activeMenu", "free");
        model.addAttribute("communityMember", userDetails.getMember());

        return "community/communityPost-list";
    }

    // 상세 조회
    @GetMapping("/{postId}")
    public String article(
            @PathVariable("postId") long postId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "schType", defaultValue = "all") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            Model model) {

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

        // 작성자는 로그인한 회원으로 설정
        dto.setAuthorId(info.getMember_id());

        if (dto.getTitle().isBlank()
                || dto.getTitle().length() > 200
                || content.isBlank()) {

            model.addAttribute("post", dto);
            model.addAttribute("mode", "write");
            model.addAttribute("pageTitle", "자유 이야기 등록");
            model.addAttribute("activeMenu", "free");
            model.addAttribute("error",
                    "제목은 1~200자로 입력하고 내용을 작성해 주세요.");

            return "community/communityPost-write";
        }

        service.insertPost(dto);

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

        // 다른 사람의 글은 수정 불가
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

        if (dto.getAuthorId() != info.getMember_id()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        dto.setTitle(title.trim());
        dto.setContent(content);
        dto.setAuthorId(info.getMember_id());

        if (dto.getTitle().isBlank()
                || dto.getTitle().length() > 200
                || content.isBlank()) {

            model.addAttribute("post", dto);
            model.addAttribute("mode", "update");
            model.addAttribute("pageTitle", "자유 이야기 수정");
            model.addAttribute("activeMenu", "free");
            model.addAttribute("error",
                    "제목은 1~200자로 입력하고 내용을 작성해 주세요.");

            return "community/communityPost-write";
        }

        if (service.updatePost(dto) == 0) {
            return "redirect:/community/free";
        }

        return "redirect:/community/free/" + postId;
    }

    // 작성자 본인의 글 삭제
    @PostMapping("/{postId}/delete")
    public String delete(
            @PathVariable("postId") long postId) {

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

    // 관리자만 게시글 숨김 가능
    @PostMapping("/{postId}/hide")
    public String hide(
            @PathVariable("postId") long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/member/login";
        }

        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth ->
                        auth.getAuthority().equals("ROLE_ADMIN"));

        // 버튼을 거치지 않은 요청도 권한 확인
        if (!admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        service.hidePost(postId);

        return "redirect:/community/free";
    }
}