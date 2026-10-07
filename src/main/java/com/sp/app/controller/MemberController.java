package com.sp.app.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sp.app.domain.dto.MemberDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member/*")
public class MemberController {

	private final MemberService service;
	
	@GetMapping("login")
	public String loginForm(@RequestParam(name = "error", required = false) String error,
			Model model) {
		
		if(error != null) {
			model.addAttribute("message", "아이디 또는 패스워드가 일치하지 않습니다.");
		}
		
		return "member/login";
	}
	
	@GetMapping("account")
	public String memberForm(Model model) {
		model.addAttribute("mode", "account");
		
		return "member/member";
	}
	
	@PostMapping("account")
	public String memberSubmit(MemberDto dto,
			final RedirectAttributes rAttr,
			Model model) {
		
		try {
			//dto.setIpAddr(RequestUtils.getClientIp());
			
			service.insertMember(dto);
			
			StringBuilder sb = new StringBuilder();
			sb.append(dto.getName()).append("님의 회원 가입이 정상적으로 처리되었습니다.<br>");
			sb.append("메인화면으로 이동하여 로그인 하시기 바랍니다.<br>");

			rAttr.addFlashAttribute("message", sb.toString());
			rAttr.addFlashAttribute("title", "회원 가입");

			return "redirect:/member/complete";
			
		} catch (DuplicateKeyException e) {
			model.addAttribute("mode", "account");
			model.addAttribute("message", "아이디 중복으로 회원가입이 실패했습니다.");
		} catch (DataIntegrityViolationException e) {
			model.addAttribute("mode", "account");
			model.addAttribute("message", "제약 조건 위반으로 회원가입이 실패했습니다.");
		} catch (Exception e) {
			log.error("memberSubmit error : ", e);
			model.addAttribute("mode", "account");
			model.addAttribute("message", "회원가입이 실패했습니다.");
		}

		return "member/member";
	}
	
	@GetMapping("complete")
	public String complete(@ModelAttribute("message") String message) throws Exception {

		if (message == null || message.isBlank()) { // F5 새로고침 대응
			return "redirect:/";
		}

		return "member/complete";
	} 
	
	@GetMapping("updatePwd")
	public String updatePwdForm() {
		return "member/updatePwd";
	}
	
	@PostMapping("updatePwd")
	public String updatePwdSubmit(
			@RequestParam(name="password") String password,
			Model model) {
		try {
			SessionInfo info = LoginMemberUtil.getSessionInfo();
			
			MemberDto dto = new MemberDto();
			dto.setLogin_id(info.getLogin_id());
			dto.setPassword(password);
			
			service.updatePassword(dto);
			
		} catch (RuntimeException e) {
			model.addAttribute("message", "변경할 패스워드가 기존 패스워드와 일치합니다.");
			return "member/updatePwd";
		} catch (Exception e) {
			log.error("updatePwdSubmit error : ", e);
			model.addAttribute("message", "패스워드 변경 실패했습니다.");
			return "member/updatePwd";
		}
		
		return "redirect:/";
	}
	
	@PostMapping("userIdCheck")
	@ResponseBody
	public ResponseEntity<?> handleUserIdCheck(@RequestParam(name = "login_id") String login_id) throws Exception {
		String p = "false";
		try {
			MemberDto dto = service.findById(login_id);
			if (dto == null) {
				p = "true";
			}
		} catch (Exception e) {
			log.error("handleUserIdCheck error : ", e);
		}
		
		return ResponseEntity.ok(Map.of("passed", p));
	}
	
	@GetMapping("pwd")
	public String pwdForm(@RequestParam(name = "dropout", required = false) String dropout, 
			Model model) {

		if (dropout == null) {
			model.addAttribute("mode", "update");
		} else {
			model.addAttribute("mode", "dropout");
		}

		return "member/pwd";
	}

	@PostMapping("pwd")
	public String pwdSubmit(@RequestParam(name = "password") String password,
			@RequestParam(name = "mode") String mode, 
			final RedirectAttributes rAttr,
			Model model) {

		try {
			SessionInfo info = LoginMemberUtil.getSessionInfo();
			MemberDto dto = Objects.requireNonNull(service.findById(info.getMember_id()));

			boolean bPwd = service.isPasswordCheck(info.getLogin_id(), password);
			
			if (!bPwd) {
				model.addAttribute("mode", mode);
				model.addAttribute("message", "패스워드가 일치하지 않습니다.");
				return "member/pwd";
			}

			// 회원 탈퇴 처리
			if (mode.equals("dropout")) {
				Map<String, Object> map = new HashMap<>();
				map.put("member_id", info.getMember_id());
				map.put("filename", info.getAvatar());

				// DB 탈퇴 처리 (개인정보 파기 및 STATUS = 3)
				service.deleteMember(map);

				// 세션 인증 정보 삭제 (로그아웃)
				LoginMemberUtil.logout();

				StringBuilder sb = new StringBuilder();
				sb.append(dto.getName()).append("님의 회원 탈퇴 처리가 정상적으로 처리되었습니다.<br>");
				sb.append("메인화면으로 이동 하시기 바랍니다.<br>");

				rAttr.addFlashAttribute("title", "회원 탈퇴");
				rAttr.addFlashAttribute("message", sb.toString());

				return "redirect:/member/complete";
			}

			// 회원 정보 수정 폼 이동
			model.addAttribute("dto", dto);
			model.addAttribute("mode", "update");
			
			return "member/member";
			
		} catch (NullPointerException e) {
			LoginMemberUtil.logout();
		} catch (Exception e) {
			log.error("pwdSubmit error : ", e);
		}
		
		return "redirect:/";
	}

	@PostMapping("update")
	public String updateSubmit(MemberDto dto,
			final RedirectAttributes rAttr,
			Model model) {

		StringBuilder sb = new StringBuilder();
		try {
			SessionInfo info = LoginMemberUtil.getSessionInfo();
			dto.setMember_id(info.getMember_id());
			
			service.updateMember(dto);
			
			// 세션 정보 최신화 (프로필 사진, 이름, 이메일)
			if (dto.getProfile_photo() != null) {
				info.setAvatar(dto.getProfile_photo());
			}
			if (dto.getName() != null) {
				info.setName(dto.getName());
			}
			if (dto.getEmail() != null) {
				info.setEmail(dto.getEmail());
			}
			
			sb.append(dto.getName()).append("님의 회원정보가 정상적으로 변경되었습니다.<br>");
			sb.append("메인화면으로 이동 하시기 바랍니다.<br>");
		} catch (Exception e) {
			log.error("updateSubmit error : ", e);
			sb.append(dto.getName()).append("님의 회원정보 변경이 실패했습니다.<br>");
			sb.append("잠시후 다시 변경 하시기 바랍니다.<br>");
		}

		rAttr.addFlashAttribute("title", "회원 정보 수정");
		rAttr.addFlashAttribute("message", sb.toString());
		
		return "redirect:/member/complete";
	}

	// 패스워드 찾기
	@GetMapping("pwdFind")
	public String pwdFindForm() throws Exception {
		SessionInfo info = LoginMemberUtil.getSessionInfo();
		
		if(info != null) {
			return "redirect:/";
		}
		
		return "member/pwdFind";
	}
	
	@PostMapping("pwdFind")
	public String pwdFindSubmit(@RequestParam(name = "login_id") String login_id,
			@RequestParam(name = "name") String name,
			final RedirectAttributes rAttr,
			Model model) throws Exception {
		
		try {
			MemberDto dto = service.findById(login_id);
			
			// 계정이 존재하지 않거나, 이메일이 없거나, 정지/탈퇴(STATUS != 1)된 계정이거나, 이름 불일치 시
			if (dto == null || dto.getEmail() == null || dto.getEnabled() != 1 || !dto.getName().equals(name)) {
				model.addAttribute("message", "등록된 정보가 없거나 이용할 수 없는 계정입니다.");
				return "member/pwdFind";
			}
			
			// 임시 패스워드 생성 및 이메일 전송
			service.generatePwd(dto);
			
			StringBuilder sb = new StringBuilder();
			sb.append("회원님의 이메일로 임시패스워드를 전송했습니다.<br>");
			sb.append("로그인 후 패스워드를 변경하시기 바랍니다.<br>");
			
			rAttr.addFlashAttribute("title", "패스워드 찾기");
			rAttr.addFlashAttribute("message", sb.toString());
			
			return "redirect:/member/complete";
			
		} catch (Exception e) {
			log.error("pwdFindSubmit error : ", e);
			model.addAttribute("message", "이메일 전송에 실패했습니다.");
		}
		
		return "member/pwdFind";
	}
	
	@DeleteMapping("deleteProfile")
	@ResponseBody
	public ResponseEntity<?> deleteProfilePhoto(@RequestParam(name = "profile_photo") String profile_photo) {
		try {
			SessionInfo info = LoginMemberUtil.getSessionInfo();
			
			if (profile_photo != null && !profile_photo.isBlank()) {
				Map<String, Object> map = new HashMap<>();
				map.put("member_id", info.getMember_id());
				map.put("filename", profile_photo);
				
				service.deleteProfilePhoto(map);
				info.setAvatar(null);
			}
			
			return ResponseEntity.ok().body(Map.of("success", true));
		} catch (Exception e) {
			log.error("deleteProfilePhoto error : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	@GetMapping("expired")
	public String expired() {
		// 세션이 만료된 경우
		return "member/expired";
	}	
}