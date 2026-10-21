package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sp.app.common.PaginateUtil;
import com.sp.app.domain.dto.CommunityQuestionDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.CommunityQuestionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/community/questions")
public class CommunityQuestionController {
    private final CommunityQuestionService service;
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
        int size = 6;

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

        List<CommunityQuestionDto> list = service.listQuestion(map);

        model.addAttribute("questions", list);
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("page", current_page);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        model.addAttribute("pageStart", Math.max(1, current_page - 2));
        model.addAttribute("pageEnd", Math.min(totalPage, current_page + 2));
        model.addAttribute("pageTitle", "요리 질문");
        model.addAttribute("activeMenu", "questions");
        model.addAttribute("communityMember", userDetails.getMember());

        return "community/communityQuestion-list";
    }

    // 상세와 이전글·다음글 조회
    @GetMapping("/{postId}")
    public String article(
            @PathVariable("postId") long postId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "schType", defaultValue = "all") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) throws Exception {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null || userDetails == null) {
            return "redirect:/member/login";
        }

        // 공개된 질문만 조회수 증가
        if (service.updateHitCount(postId) == 0) {
            return "redirect:/community/questions";
        }

        CommunityQuestionDto dto = service.findById(postId);

        if (dto == null) {
            return "redirect:/community/questions";
        }

        if (!schType.equals("all") && !schType.equals("title")
                && !schType.equals("content")) {
            schType = "all";
        }

        kwd = kwd.trim();

        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("schType", schType);
        map.put("kwd", kwd);

        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        model.addAttribute("prevQuestion", service.findByPrev(map));
        model.addAttribute("nextQuestion", service.findByNext(map));
        model.addAttribute("question", dto);
        model.addAttribute("page", Math.max(1, page));
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);

        // 수정·삭제와 관리자 숨김 버튼 표시
        model.addAttribute("owner", dto.getAuthorId() == info.getMember_id());
        model.addAttribute("admin", admin);

        model.addAttribute("pageTitle", "요리 질문 상세");
        model.addAttribute("activeMenu", "questions");
        model.addAttribute("communityMember", info);

        return "community/communityQuestion-article";
    }

    // 등록 화면
    @GetMapping("/write")
    public String writeForm(Model model) {
        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        model.addAttribute("question", new CommunityQuestionDto());
        model.addAttribute("mode", "write");
        model.addAttribute("pageTitle", "요리 질문 등록");
        model.addAttribute("activeMenu", "questions");

        return "community/communityQuestion-write";
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

        CommunityQuestionDto dto = new CommunityQuestionDto();
        dto.setTitle(title.trim());
        dto.setContent(content);

        // 작성자 번호는 로그인 정보 사용
        dto.setAuthorId(info.getMember_id());

        model.addAttribute("question", dto);
        model.addAttribute("mode", "write");
        model.addAttribute("pageTitle", "요리 질문 등록");
        model.addAttribute("activeMenu", "questions");

        if (dto.getTitle().isBlank() || dto.getTitle().length() > 200
                || content.isBlank()) {
            model.addAttribute("error",
                    "제목은 1~200자로 입력하고 내용을 작성해 주세요.");
            return "community/communityQuestion-write";
        }

        try {
            service.insertQuestion(dto);
        } catch (Exception e) {
            log.info("writeSubmit : ", e);
            model.addAttribute("error", "질문 등록에 실패했습니다.");
            return "community/communityQuestion-write";
        }

        return "redirect:/community/questions";
    }

    // 본인 질문 수정 화면
    @GetMapping("/{postId}/update")
    public String updateForm(
            @PathVariable("postId") long postId,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityQuestionDto dto = service.findById(postId);

        if (dto == null) {
            return "redirect:/community/questions";
        }

        if (dto.getAuthorId() != info.getMember_id()) {
            return "redirect:/community/questions";
        }

        model.addAttribute("question", dto);
        model.addAttribute("mode", "update");
        model.addAttribute("pageTitle", "요리 질문 수정");
        model.addAttribute("activeMenu", "questions");

        // 등록·수정은 같은 HTML 사용
        return "community/communityQuestion-write";
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

        CommunityQuestionDto dto = service.findById(postId);

        if (dto == null) {
            return "redirect:/community/questions";
        }

        // 주소로 직접 요청해도 작성자 확인
        if (dto.getAuthorId() != info.getMember_id()) {
            return "redirect:/community/questions";
        }

        dto.setTitle(title.trim());
        dto.setContent(content);
        dto.setAuthorId(info.getMember_id());

        model.addAttribute("question", dto);
        model.addAttribute("mode", "update");
        model.addAttribute("pageTitle", "요리 질문 수정");
        model.addAttribute("activeMenu", "questions");

        if (dto.getTitle().isBlank() || dto.getTitle().length() > 200
                || content.isBlank()) {
            model.addAttribute("error",
                    "제목은 1~200자로 입력하고 내용을 작성해 주세요.");
            return "community/communityQuestion-write";
        }

        try {
            if (service.updateQuestion(dto) == 0) {
                return "redirect:/community/questions";
            }
        } catch (Exception e) {
            log.info("updateSubmit : ", e);
            model.addAttribute("error", "질문 수정에 실패했습니다.");
            return "community/communityQuestion-write";
        }

        return "redirect:/community/questions/" + postId;
    }

    // 본인 질문 삭제
    @PostMapping("/{postId}/delete")
    public String delete(
            @PathVariable("postId") long postId) throws Exception {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityQuestionDto dto = service.findById(postId);

        if (dto == null) {
            return "redirect:/community/questions";
        }

        if (dto.getAuthorId() != info.getMember_id()) {
            return "redirect:/community/questions";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("authorId", info.getMember_id());

        // 실제 행은 유지하고 XML에서 STATUS = 2로 변경
        service.deleteQuestion(map);

        return "redirect:/community/questions";
    }

    // 관리자 질문 숨김
    @PostMapping("/{postId}/hide")
    public String hide(
            @PathVariable("postId") long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails)
            throws Exception {

        if (userDetails == null) {
            return "redirect:/member/login";
        }

        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        if (!admin) {
            return "redirect:/community/questions";
        }

        service.hideQuestion(postId);

        return "redirect:/community/questions";
    }
}