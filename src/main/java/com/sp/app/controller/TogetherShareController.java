package com.sp.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	public String list(Model model) {
	    model.addAttribute("shareTypeOptions", Map.of(
	            0, "무료 나눔",
	            1, "교환"
	    ));

		return "together/share/list";
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
	
}