package com.sp.app.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.domain.dto.TogetherShareDto;
import com.sp.app.domain.dto.TogetherShareItemDto;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.TogetherShareService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TogetherShareController {

    private final TogetherShareService service;

    // 재료나눔 화면
    @GetMapping({"/together/share", "/together/share/", "/community/share"})
    public String list(Model model) throws Exception {
        SessionInfo info = LoginMemberUtil.getSessionInfo();

        int size = 10;

        List<TogetherShareDto> posts = service.listShare(0 , size + 1);
        boolean hasMore = posts.size() > size;

        if(hasMore) {
            posts = posts.subList(0, size);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("hasMore", hasMore);
        model.addAttribute("nextOffset", posts.size());
        model.addAttribute("shareTypeOptions", Map.of(
                0, "무료 나눔",
                1, "교환"
        ));
        model.addAttribute("loginMemberId",
                info == null ? null : info.getMember_id()
        );

        return "together/share/list";
    }

    // 재료나눔 글 더보기
    @GetMapping("/together/share/more")
    @ResponseBody
    public Map<String, Object> more (
            @RequestParam(name = "offset", defaultValue = "0") int offset) throws Exception {

        int size = 10;

        List<TogetherShareDto> posts = service.listShare(offset, size + 1);
        boolean hasMore = posts.size() > size;

        if(hasMore) {
            posts = posts.subList(0, size);
        }

        return Map.of(
                "posts" , posts,
                "hasMore", hasMore,
                "nextOffset" , offset + posts.size()
                );
    }

    // 글 등록
    @PostMapping("/together/share/write")
    public String writeSubmit(
            @ModelAttribute TogetherShareDto dto,
            RedirectAttributes redirectAttributes) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if(info == null) {
            return "redirect:/member/login";
        }

        try {
            dto.setAuthor_id(info.getMember_id());
            dto.setStatus(0);
            dto.setShare_status(0);

            service.insertShare(dto);

            redirectAttributes.addFlashAttribute(
                    "message", "나눔 글이 등록되었습니다.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "message", e.getMessage());
        } catch (Exception e) {
            log.error("재료나눔 글 등록 오류", e);
            redirectAttributes.addFlashAttribute(
                    "message", "나눔 글 등록 중 오류가 발생했습니다.");
        }

        return "redirect:/together/share";
    }

    // 재료명 / 별칭 검색
    @GetMapping("/together/share/ingredients/search")
    @ResponseBody
    public List<TogetherShareItemDto> searchIngredients(
            @RequestParam(name = "keyword", defaultValue = "") String keyword) throws Exception {

        return service.searchIngredients(keyword);
    }

    // 분류별 재료 목록
    @GetMapping("/together/share/ingredients/by-type")
    @ResponseBody
    public List<TogetherShareItemDto> listIngredientByType(
            @RequestParam("type") String type) throws Exception {

        return service.listIngredientsByType(type);
    }

    // 재료나눔 글 상세보기
    @GetMapping("/together/share/detail/{share_id}")
    public String detail(
            @PathVariable("share_id") Long share_id,
            Model model) throws Exception {

        TogetherShareDto dto = service.findShareById(share_id);

        // 없거나 삭제된 글이면 목록으로 리다이렉트
        if(dto == null) {
            return "redirect:/together/share";
        }

        // 로그인한 회원이 작성자인지 확인
        SessionInfo info = LoginMemberUtil.getSessionInfo();

        boolean isAuthor = info != null && Objects.equals(dto.getAuthor_id(), info.getMember_id());

        model.addAttribute("share", dto);
        model.addAttribute("items", service.listShareItems(share_id));
        model.addAttribute("isAuthor", isAuthor);

        return "together/share/detail";
    }
}
