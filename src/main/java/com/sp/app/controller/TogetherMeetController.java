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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sp.app.domain.dto.SessionInfo;
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

	@GetMapping({ "/together/meet", "/together/meet/", "/community/meet" })
	public String list(Model model) throws Exception {
		// DB에서 조회한 목록을 HTML에 전달
		model.addAttribute("posts", service.listMeet());

		model.addAttribute("statusLabels", Map.of(0, "모집 중", 1, "모집 마감", 2, "모임 완료", 3, "모임 취소"));

		return "together/meet/list";
	}

	@PostMapping("/together/meet/write")
	public String writeSubmit(@ModelAttribute TogetherMeetDto dto) throws Exception {

		try {
			SessionInfo info = LoginMemberUtil.getSessionInfo();
			if (info == null) {
				return "redirect:/member/login";
			}
			dto.setAuthor_id(info.getMember_id());
			dto.setStatus(0);
			dto.setMeet_status(0);

			service.insertMeet(dto);

		} catch (Exception e) {
			log.error("같이먹어요 글 작성 오류.", e);
		}

		return "redirect:/together/meet";
	}

	@GetMapping("/together/meet/detail/{meet_id}")
	public String detail(@PathVariable("meet_id") Long meet_id, Model model) throws Exception {
		TogetherMeetDto dto = service.findMeetById(meet_id);

		if (dto == null) {
			return "redirect:/together/meet";
		}
		
		// 로그인한 회원이 글의 작성자인지 확인
		SessionInfo info = LoginMemberUtil.getSessionInfo();

		boolean isAuthor = info != null && Objects.equals(dto.getAuthor_id(), info.getMember_id());

		try {
			model.addAttribute("meet", dto);
			model.addAttribute("isAuthor", isAuthor);
			model.addAttribute("participantCount", service.countMeetParticipants(meet_id));
			if (isAuthor) {
				
				  var applications = service.listParticipant(meet_id);

				    log.info("신청자 조회: 글번호={}, 신청자수={}",
				            meet_id, applications.size());
				model.addAttribute("applications", service.listParticipant(meet_id));
				model.addAttribute("approvalActionReady" , true);
				model.addAttribute("rejectionActionReady", true);
			}

		} catch (Exception e) {
			log.error("같이먹어요 글 상세보기 오류.", e);
		}

		return "together/meet/detail";
	}

	@PostMapping("/together/meet/{meet_id}/apply")
	public String apply(@PathVariable("meet_id") Long meet_id,
			RedirectAttributes redirectAttributes) throws Exception {
		SessionInfo info = LoginMemberUtil.getSessionInfo();

		if (info == null) {
			return "redirect:/member/login";
		}

		try {
		    service.createMeetParticipants(meet_id, info.getMember_id());

		    redirectAttributes.addFlashAttribute(
		            "applyMessage", "참여 신청이 완료되었습니다.");

		} catch (IllegalArgumentException | IllegalStateException e) {
		    redirectAttributes.addFlashAttribute(
		            "applyMessage", e.getMessage());

		} catch (DuplicateKeyException e) {
		    redirectAttributes.addFlashAttribute(
		            "applyMessage", "이미 참여 신청한 모임입니다.");

		} catch (Exception e) {
		    log.error("같이먹어요 참여신청 오류.", e);

		    redirectAttributes.addFlashAttribute(
		            "applyMessage", "참여 신청 중 오류가 발생했습니다.");
		}

		return "redirect:/together/meet/detail/" + meet_id;
	}
	
	@PostMapping("/together/meet/{meet_id}/participants/{member_id}/approve")
	public String approve(@PathVariable("meet_id") Long meet_id, @PathVariable("member_id") Long member_id,
			RedirectAttributes redirectAttributes) {
		
		SessionInfo info = LoginMemberUtil.getSessionInfo();
		
		if(info == null) {
			return "redirect:/member/login";
		}
		
		try {
			service.approveMeetParticipant(meet_id, member_id , info.getMember_id());
			
			redirectAttributes.addFlashAttribute(
					"applicationMessage","참여 신청을 승인했습니다.");
			
		} catch (IllegalArgumentException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute(
					"applicationMessage" , e.getMessage());
		} catch (Exception e) {
			log.error("같이 먹어요 참여 승인 오류" , e);
			redirectAttributes.addFlashAttribute(
					"applicationMessage" , "승인 처리 중 오류가 발생했습니다.");
		}
		return "redirect:/together/meet/detail/" + meet_id;
	}
	
	@PostMapping("/together/meet/{meet_id}/participants/{member_id}/reject")
	public String reject(@PathVariable("meet_id") Long meet_id, @PathVariable("member_id") Long member_id,
			RedirectAttributes redirectAttributes) {
		
		SessionInfo info = LoginMemberUtil.getSessionInfo();
		
		if(info == null) {
			return "redirect:/member/login";
		}
		
		try {
			service.rejectMeetParticipant(meet_id, member_id , info.getMember_id());
			
			redirectAttributes.addFlashAttribute(
					"applicationMessage","참여 신청을 거절했습니다.");
			
		} catch (IllegalArgumentException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute(
					"applicationMessage" , e.getMessage());
		} catch (Exception e) {
			log.error("같이 먹어요 참여 거절 오류" , e);
			redirectAttributes.addFlashAttribute(
					"applicationMessage" , "거절 처리 중 오류가 발생했습니다.");
		}
		return "redirect:/together/meet/detail/" + meet_id;
	}
}
