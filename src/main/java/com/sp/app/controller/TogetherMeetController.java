package com.sp.app.controller;

import java.util.Map;
import java.util.Objects;

import org.springframework.dao.DuplicateKeyException;
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
import com.sp.app.domain.dto.TogetherMeetCommentDto;
import com.sp.app.domain.dto.TogetherMeetDto;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.TogetherMeetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TogetherMeetController {

    private final TogetherMeetService service;

    // 게시글

    // 글 목록 - 처음 10개
    @GetMapping({"/together/meet", "/together/meet/", "/community/meet"})
    public String list(Model model) throws Exception {

        // 11번째 글로 다음 목록 확인

        var rows = service.listMeet(0, 11);
        var posts = rows.subList(0, Math.min(rows.size(), 10));

        model.addAttribute("posts", posts);
        model.addAttribute("hasMore", rows.size() > 10);
        model.addAttribute("nextOffset", posts.size());

        model.addAttribute("statusLabels", Map.of(0, "모집 중", 1, "모집 마감", 2, "모임 완료", 3, "모임 취소"));

        return "together/meet/list";
    }

    // 글 목록 더보기
    @GetMapping("/together/meet/more")
    @ResponseBody
    public Map<String, Object> more(@RequestParam(defaultValue = "0") int offset) throws Exception {

        if (offset < 0) {
            throw new IllegalArgumentException("잘못된 조회 위치입니다.");
        }

        // 11번째 글로 다음 목록 확인

        var rows = service.listMeet(offset, 11);
        boolean hasMore = rows.size() > 10;

        var posts = rows.subList(0, Math.min(rows.size(), 10));

        return Map.of("posts", posts, "hasMore", hasMore, "nextOffset", offset + posts.size());
    }

    // 글 상세보기
    @GetMapping("/together/meet/detail/{meet_id}")
    public String detail(@PathVariable("meet_id") Long meet_id, Model model) throws Exception {
        TogetherMeetDto dto = service.findMeetById(meet_id);

        if (dto == null) {
            return "redirect:/together/meet";
        }

        // 로그인 정보

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        // 로그인 회원이 작성자인지 확인
        boolean isAuthor = info != null && Objects.equals(dto.getAuthor_id(), info.getMember_id());

        try {
            model.addAttribute("meet", dto);
            model.addAttribute("isAuthor", isAuthor);
            model.addAttribute("participantCount", service.countMeetParticipants(meet_id));
            model.addAttribute("participants", service.listAcceptedParticipants(meet_id));

            // 댓글 10개 + 다음 댓글 확인
            var commentRows = service.listMeetComment(meet_id, 0, 11);
            var comments = commentRows.subList(0, Math.min(commentRows.size(), 10));

            model.addAttribute("comments", comments);
            model.addAttribute("commentCount", service.countMeetComments(meet_id));
            model.addAttribute("commentHasMore", commentRows.size() > 10);
            model.addAttribute("commentNextOffset", comments.size());
            // 작성자만 신청 관리 표시
            if (isAuthor) {

                var applications = service.listParticipant(meet_id);

                log.info("신청자 조회: 글번호={}, 신청자수={}", meet_id, applications.size());
                model.addAttribute("applications", applications);
                model.addAttribute("approvalActionReady", true);
                model.addAttribute("rejectionActionReady", true);
            }

        } catch (Exception e) {
            log.error("같이먹어요 글 상세보기 오류.", e);
        }

        return "together/meet/detail";
    }

    // 글 등록
    @PostMapping("/together/meet/write")
    public String writeSubmit(@ModelAttribute TogetherMeetDto dto, RedirectAttributes redirectAttributes)
            throws Exception {

        try {
            // 로그인 정보
            SessionInfo info = LoginMemberUtil.getSessionInfo();
            if (info == null) {
                return "redirect:/member/login";
            }
            dto.setAuthor_id(info.getMember_id());
            dto.setStatus(0);
            dto.setMeet_status(0);

            service.insertMeet(dto);
            redirectAttributes.addFlashAttribute("message", "게시글이 등록되었습니다.");

        } catch (Exception e) {
            log.error("같이먹어요 글 작성 오류.", e);
        }

        return "redirect:/together/meet";
    }

    // 글 수정 화면
    @GetMapping("/together/meet/{meet_id}/edit")
    public String edit(@PathVariable("meet_id") Long meet_id, Model model,
            RedirectAttributes redirectAttributes) throws Exception {
        // 로그인 정보
        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        TogetherMeetDto dto = service.findMeetById(meet_id);

        if (dto == null) {
            return "redirect:/together/meet";
        }

        if (!Objects.equals(dto.getAuthor_id(), info.getMember_id())) {
            redirectAttributes.addFlashAttribute("applyMessage", "글 작성자만 수정할 수 있습니다.");

            return "redirect:/together/meet/detail/" + meet_id;
        }

        model.addAttribute("meet", dto);

        return "together/meet/edit";
    }

    // 글 수정 저장
    @PostMapping("/together/meet/{meet_id}/edit")
    public String editSubmit(@PathVariable("meet_id") Long meet_id, @ModelAttribute TogetherMeetDto dto,
            RedirectAttributes redirectAttributes) {

        // 로그인 정보

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        try {
            // 글 번호는 주소에서 가져오기
            dto.setMeet_id(meet_id);
            dto.setAuthor_id(info.getMember_id());

            service.updateMeet(dto);

            redirectAttributes.addFlashAttribute("message", "게시글을 수정했습니다.");

            return "redirect:/together/meet";

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("applyMessage", e.getMessage());
        } catch (Exception e) {
            log.error("같이 먹어요 글 수정 오류", e);
            redirectAttributes.addFlashAttribute("applyMessage", "글 수정 중 오류가 발생했습니다.");
        }

        return "redirect:/together/meet/" + meet_id + "/edit";

    }

    // 글 삭제
    @PostMapping("/together/meet/{meet_id}/delete")
    public String delete(@PathVariable("meet_id") Long meet_id, RedirectAttributes redirectAttributes) {

        // 로그인 정보

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        try {
            service.deleteMeet(meet_id, info.getMember_id());

            redirectAttributes.addFlashAttribute("message", "게시글을 삭제했습니다.");

            return "redirect:/together/meet";

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("applyMessage", e.getMessage());
        } catch (Exception e) {
            log.error("같이먹어요 글 삭제 오류", e);
            redirectAttributes.addFlashAttribute("applyMessage", "글 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/together/meet/detail" + meet_id;
    }

    // 참여 신청

    // 참여 신청
    @PostMapping("/together/meet/{meet_id}/apply")
    public String apply(@PathVariable("meet_id") Long meet_id, RedirectAttributes redirectAttributes)
            throws Exception {
        // 로그인 정보
        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        try {
            service.createMeetParticipants(meet_id, info.getMember_id());

            redirectAttributes.addFlashAttribute("applyMessage", "참여 신청이 완료되었습니다.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("applyMessage", e.getMessage());

        } catch (DuplicateKeyException e) {
            redirectAttributes.addFlashAttribute("applyMessage", "이미 참여 신청한 모임입니다.");

        } catch (Exception e) {
            log.error("같이먹어요 참여신청 오류.", e);

            redirectAttributes.addFlashAttribute("applyMessage", "참여 신청 중 오류가 발생했습니다.");
        }

        return "redirect:/together/meet/detail/" + meet_id;
    }

    // 참여 신청 승인
    @PostMapping("/together/meet/{meet_id}/participants/{member_id}/approve")
    public String approve(@PathVariable("meet_id") Long meet_id, @PathVariable("member_id") Long member_id,
            RedirectAttributes redirectAttributes) {

        // 로그인 정보

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        try {
            service.approveMeetParticipant(meet_id, member_id, info.getMember_id());

            redirectAttributes.addFlashAttribute("applicationMessage", "참여 신청을 승인했습니다.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("applicationMessage", e.getMessage());
        } catch (Exception e) {
            log.error("같이 먹어요 참여 승인 오류", e);
            redirectAttributes.addFlashAttribute("applicationMessage", "승인 처리 중 오류가 발생했습니다.");
        }
        return "redirect:/together/meet/detail/" + meet_id;
    }

    // 참여 신청 거절
    @PostMapping("/together/meet/{meet_id}/participants/{member_id}/reject")
    public String reject(@PathVariable("meet_id") Long meet_id, @PathVariable("member_id") Long member_id,
            RedirectAttributes redirectAttributes) {

        // 로그인 정보

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        try {
            service.rejectMeetParticipant(meet_id, member_id, info.getMember_id());

            redirectAttributes.addFlashAttribute("applicationMessage", "참여 신청을 거절했습니다.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("applicationMessage", e.getMessage());
        } catch (Exception e) {
            log.error("같이 먹어요 참여 거절 오류", e);
            redirectAttributes.addFlashAttribute("applicationMessage", "거절 처리 중 오류가 발생했습니다.");
        }
        return "redirect:/together/meet/detail/" + meet_id;
    }

    // 댓글

    // 댓글 등록
    @PostMapping("/together/meet/{meet_id}/comments")
    public String insertComment(@PathVariable("meet_id") Long meet_id,
            @ModelAttribute TogetherMeetCommentDto dto, RedirectAttributes redirectAttributes) {

        // 로그인 정보

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        try {
            // 글 번호는 주소에서 가져오기
            dto.setMeet_id(meet_id);
            dto.setMember_id(info.getMember_id());

            service.insertMeetComment(dto);

            redirectAttributes.addFlashAttribute("applyMessage", "댓글이 등록 되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("applyMessage", e.getMessage());
        } catch (Exception e) {
            log.error("같이 먹어요 댓글 등록 오류", e);
            redirectAttributes.addFlashAttribute("applyMessage", "댓글 등록 중 오류가 발생했습니다.");
        }

        return "redirect:/together/meet/detail/" + meet_id;
    }

    // 댓글 더보기
    @GetMapping("/together/meet/{meet_id}/comments/more")
    @ResponseBody
    public Map<String, Object> moreComments(@PathVariable("meet_id") Long meet_id,
            @RequestParam(name = "offset", defaultValue = "0") int offset) throws Exception {

        if (offset < 0) {
            throw new IllegalArgumentException("잘못된 조회 위치입니다.");
        }

        // 댓글 10개 + 다음 댓글 확인

        var rows = service.listMeetComment(meet_id, offset, 11);
        var comments = rows.subList(0, Math.min(rows.size(), 10));

        return Map.of("comments", comments, "hasMore", rows.size() > 10, "nextOffset",
                offset + comments.size());
    }
}
