package com.sp.app.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.common.StorageService;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.mail.Mail;
import com.sp.app.mail.MailSender;
import com.sp.app.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

	private final MemberMapper mapper;
	private final StorageService storageService;
	private final MailSender mailSender;
	private final PasswordEncoder bcryptEncoder;
	
	@Value("${file.upload-root}/member")
	private String uploadPath;
	
	@Override
	public MemberDto loginSnsMember(Map<String, Object> map) {
		MemberDto dto = null;

		try {
			dto = mapper.loginSnsMember(map);
		} catch (Exception e) {
			log.info("loginSnsMember : ", e);
		}

		return dto;
	}

	@Transactional(rollbackFor = {Exception.class})
	@Override
	public void insertMember(MemberDto dto) throws Exception {
		try {
			// 프로필 사진 업로드 처리
			if(dto.getSelectFile() != null && !dto.getSelectFile().isEmpty()) {
				String saveFilename = storageService.uploadFileToServer(dto.getSelectFile(), uploadPath);
				dto.setProfile_photo(saveFilename);
			}			

			// 패스워드 암호화
			String encPassword = bcryptEncoder.encode(dto.getPassword());
			dto.setPassword(encPassword);
						
			// 기본 권한 설정 (1: ADMIN, 2: USER - DB ROLE_ID 기준)
			if(dto.getRole_id() == 0) {
				dto.setRole_id(2L); // 기본 일반 회원 권한
			}
			
			mapper.insertMember(dto);
			
		} catch (Exception e) {
			log.info("insertMember : ", e);
			throw e;
		}
	}

	@Transactional(rollbackFor = {Exception.class})
	@Override
	public void insertSnsMember(MemberDto dto) throws Exception {
		try {
			if(dto.getRole_id() == 0) {
				dto.setRole_id(2L);
			}
			mapper.insertSnsMember(dto);
		} catch (Exception e) {
			log.info("insertSnsMember : ", e);
			throw e;
		}
	}

	@Override
	public void updatePassword(MemberDto dto) throws Exception {
		if( isPasswordCheck(dto.getLogin_id(), dto.getPassword()) ) {
			throw new RuntimeException("패스워드가 기존 패스워드와 일치합니다.");
		}

		try {
			String encPassword = bcryptEncoder.encode(dto.getPassword());
			dto.setPassword(encPassword);
			
			mapper.updateMemberPassword(dto);
		} catch (Exception e) {
			log.info("updatePassword : ", e);
			throw e;
		}
	}

	@Override
	public void updateMemberEnabled(Map<String, Object> map) throws Exception {
		try {
			
			mapper.updateMemberStatus(map);
		} catch (Exception e) {
			log.info("updateMemberEnabled : ", e);
			throw e;
		}
	}

	@Transactional(rollbackFor = {Exception.class})
	@Override
	public void updateMember(MemberDto dto) throws Exception {
		try {
			// 업로드한 파일이 존재하는 경우
			if(dto.getSelectFile() != null && !dto.getSelectFile().isEmpty()) {
				if(dto.getProfile_photo() != null && !dto.getProfile_photo().isBlank()) {
					storageService.deleteFile(uploadPath, dto.getProfile_photo());
				}
				
				String saveFilename = storageService.uploadFileToServer(dto.getSelectFile(), uploadPath);
				dto.setProfile_photo(saveFilename);
			}			
			
			// 패스워드가 입력되었고, 기존 패스워드와 다른 경우 암호화
			if(dto.getPassword() != null && !dto.getPassword().isBlank()) {
				boolean bPwdUpdate = !isPasswordCheck(dto.getLogin_id(), dto.getPassword());
				if(bPwdUpdate) {
					String encPassword = bcryptEncoder.encode(dto.getPassword());
					dto.setPassword(encPassword);
				}
			}
			
			mapper.updateMember(dto);
			
		} catch (Exception e) {
			log.info("updateMember : ", e);
			throw e;
		}
	}

	@Override
	public void updateLastLogin(Long member_id) throws Exception {
		try {
			mapper.updateLastLogin(member_id);
		} catch (Exception e) {
			log.info("updateLastLogin : ", e);
			throw e;
		}
	}

	@Override
	public void updateLastLogin(String login_id) throws Exception {
		try {
			mapper.updateLastLoginId(login_id);
		} catch (Exception e) {
			log.info("updateLastLoginId : ", e);
			throw e;
		}
	}

	@Override
	public MemberDto findById(Long member_id) {
		MemberDto dto = null;

		try {
			dto = Objects.requireNonNull(mapper.findById(member_id));
		} catch (NullPointerException e) {
		} catch (Exception e) {
			log.info("findById : ", e);
		}

		return dto;
	}

	@Override
	public MemberDto findById(String login_id) {
		MemberDto dto = null;

		try {
			dto = Objects.requireNonNull(mapper.findByLoginId(login_id));
		} catch (NullPointerException e) {
		} catch (Exception e) {
			log.info("findById : ", e);
		}

		return dto;
	}

	@Override
	public Long getMemberId(String login_id) {
		try {
			Long result = Objects.requireNonNull(mapper.getMemberId(login_id));
			return result;
		} catch (Exception e) {
			log.info("getMemberId : ", e);
		}

		return 0L;
	}
	
	@Override
	public int checkFailureCount(String login_id) {
		int result = 0;
		
		try {
			result = mapper.checkFailureCount(login_id);
		} catch (Exception e) {
			log.info("checkFailureCount : ", e);
		}
		
		return result;
	}

	@Override
	public void updateFailureCountReset(String login_id) throws Exception {
		try {
			mapper.updateFailureCountReset(login_id);
		} catch (Exception e) {
			log.info("updateFailureCountReset : ", e);
			throw e;
		}
	}

	@Override
	public void updateFailureCount(String login_id) throws Exception {
		try {
			mapper.updateFailureCount(login_id);
		} catch (Exception e) {
			log.info("updateFailureCount : ", e);
			throw e;
		}
	}

	@Transactional(rollbackFor = {Exception.class})
	@Override
	public void deleteMember(Map<String, Object> map) throws Exception {
		try {
			// 상태값을 탈퇴/비활성(STATUS = 3 또는 0)으로 변경하는 소프트 삭제 방식
			map.put("status", 3); // 3: 탈퇴 상태
			mapper.updateMemberStatus(map);			
			
			String filename = (String)map.get("filename");
			if(filename != null && !filename.isBlank()) {
				storageService.deleteFile(uploadPath, filename);
			}
			
		} catch (Exception e) {
			log.info("deleteMember : ", e);
			throw e;
		}
	}

	@Override
	public void deleteProfilePhoto(Map<String, Object> map) throws Exception {
		try {
			String filename = (String)map.get("filename");
			if(filename != null && !filename.isBlank()) {
				storageService.deleteFile(uploadPath, filename);
			}
			
			mapper.deleteProfilePhoto(map);
		} catch (Exception e) {
			log.info("deleteProfilePhoto : ", e);
			throw e;
		}
	}

	@Override
	public void generatePwd(MemberDto dto) throws Exception {
		String lowercase = "abcdefghijklmnopqrstuvwxyz";
		String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String digits = "0123456789";
		String special_characters = "!#@$%^&*()-_=+[]{}?";
		String all_characters = lowercase + digits + uppercase + special_characters;
		
		try {
			SecureRandom random = new SecureRandom();
			StringBuilder sb = new StringBuilder();
			
			sb.append(lowercase.charAt(random.nextInt(lowercase.length())));
			sb.append(uppercase.charAt(random.nextInt(uppercase.length())));
			sb.append(digits.charAt(random.nextInt(digits.length())));
			sb.append(special_characters.charAt(random.nextInt(special_characters.length())));
			
			for(int i = sb.length(); i < 10; i++) {
				int index = random.nextInt(all_characters.length());
				sb.append(all_characters.charAt(index));
			}
			
			StringBuilder password = new StringBuilder();
			while (sb.length() > 0) {
				int index = random.nextInt(sb.length());
				password.append(sb.charAt(index));
				sb.deleteCharAt(index);
			}
	        
			String result = dto.getName() + "님의 새로 발급된 임시 패스워드는 <b> "
					+ password.toString() + " </b> 입니다.<br>"
					+ "로그인 후 반드시 패스워드를 변경하시기 바랍니다.";
			
			Mail mail = new Mail();
			mail.setReceiverEmail(dto.getEmail());
			mail.setSenderEmail("메일설정이메일@도메인");
			mail.setSenderName("관리자");
			mail.setSubject("임시 패스워드 발급");
			mail.setContent(result);
			
			String encPassword = bcryptEncoder.encode(password.toString());
			dto.setPassword(encPassword);
			mapper.updateMemberPassword(dto);
			
			mapper.updateFailureCountReset(dto.getLogin_id());
			
			boolean b = mailSender.mailSend(mail);
			if(!b) {
				throw new Exception("이메일 전송중 오류가 발생했습니다.");
			}

		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public List<MemberDto> listFindMember(Map<String, Object> map) {
		List<MemberDto> list = null;
		
		try {
			list = mapper.listFindMember(map);
		} catch (Exception e) {
			log.info("listFindMember : ", e);
		}
		
		return list;
	}

	@Override
	public String findByAuthority(String login_id) {
		String authority = null;
		
		try {
			authority = mapper.findAuthorityByLoginId(login_id);
		} catch (Exception e) {
			log.info("findByAuthority : ", e);
		}
		
		return authority;
	}

	@Override
	public boolean isPasswordCheck(String login_id, String rawPassword) {
		MemberDto dto = Objects.requireNonNull(findById(login_id));
	    
	    if (dto == null || dto.getPassword() == null) {
	        log.error("DB에서 회원 정보 또는 비밀번호를 불러오지 못함");
	        return false;
	    }

	    log.info("입력된 평문 비밀번호: {}", rawPassword);
	    log.info("DB에 저장된 비밀번호 (길이: {}): {}", dto.getPassword().length(), dto.getPassword());

	    boolean isMatch = bcryptEncoder.matches(rawPassword, dto.getPassword());
	    log.info("비밀번호 일치 여부: {}", isMatch);

	    return isMatch;
	}

}