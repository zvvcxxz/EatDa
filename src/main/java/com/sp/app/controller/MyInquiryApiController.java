package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.domain.dto.MyInquiryDto;
import com.sp.app.domain.dto.MyInquiryDto.InquiryFileDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MyInquiryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
@Slf4j
public class MyInquiryApiController {
	
	private final MyInquiryService service;
	//private final MyUtil myUtil;
	
	private Long getMemberId() {
		SessionInfo info = LoginMemberUtil.getSessionInfo();
		if(info ==null || info.getMember_id() ==null) {
			throw new IllegalStateException("로그인이 필요한 서비스입니다.");
		}
		return info.getMember_id();
	}
	
	//목록, 검색, 카테고리, 페이징
	@GetMapping
	public ResponseEntity<?> list(
			@RequestParam(name="page",defaultValue = "1") int page,
			@RequestParam(name="schType",defaultValue = "all") String schType,
			@RequestParam(name="kwd",defaultValue = "") String kwd,
			@RequestParam(required = false) Integer inquiryType) {
		
		try {
			Long memberId = getMemberId();
			Map<String, Object> map = new HashMap<>();
			
			//kwd = myUtil.decodeUrl(kwd);
			int size = 5;
			
			map.put("memberId", memberId);
			map.put("inquiryType", inquiryType);
			map.put("schType", schType);
			map.put("kwd", kwd);

			long totalCount = service.countMyInquiryList(map);
			long totalPage = (totalCount+size-1)/size;
			page = Math.max(page, 1);
			page = (int)Math.min(page, totalPage);
			if(totalCount <=0) page=1;
			
			int offset = (page -1) * size;
			if(offset <0) offset=0;
			
			
			map.put("page", page);
			map.put("size", size);
			map.put("totalCount", totalCount);
			map.put("totalPage", totalPage);
			map.put("offset", offset);
			
			List<MyInquiryDto> list = service.selectMyInquiryList(map);
			map.put("list", list);
			map.remove("memberId");
			return ResponseEntity.ok(map);
		} catch (Exception e) {
			log.info("list : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
		
		
	}
	
    // 문의 상세
    @GetMapping("/{inquiryId}")
    public ResponseEntity<?> detail(@PathVariable Long inquiryId) {
    	
    	try {
    		Long memberId = getMemberId();
    		return ResponseEntity.ok(service.selectMyInquiryDetail(inquiryId, memberId));
		} catch (Exception e) {
			log.info("detail : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		
		}
        
    }

    // 추가문의 목록
    @GetMapping("/{inquiryId}/follow-ups")
    public ResponseEntity<?> followUps(@PathVariable Long inquiryId) {
        // 원문 소유권 검사 후 추가문의 목록 반환
    	
    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
    		return ResponseEntity.ok(service.selectFollowUpInquiries(inquiryId, memberId));
		} catch (Exception e) {
			log.info("list : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }

    // 추가문의 상세
    @GetMapping("/{inquiryId}/follow-ups/{followUpId}")
    public ResponseEntity<?> followUpDetail(@PathVariable Long inquiryId,
            @PathVariable Long followUpId) {
        // 원문 ID, 추가문의 ID, 회원 ID를 함께 검증 (주소창 조작해서 조회하는거 방지)
    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
    		return ResponseEntity.ok(service.selectFollowUpDetail(inquiryId, followUpId, memberId));
		} catch (Exception e) {
			log.info("followUpDetail : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }

    // 문의 등록
    @PostMapping
    public ResponseEntity<?> create(@ModelAttribute MyInquiryDto dto) {
    	try {
    		Long memberId = getMemberId();
			dto.setMemberId(memberId);
			dto.setParentInquiryId(null);
    		return ResponseEntity.ok(service.insertMyInquiry(dto));
		} catch (Exception e) {
			log.info("create : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }

    // 문의 수정
    @PostMapping("/{inquiryId}")
    public ResponseEntity<?> update(@PathVariable Long inquiryId,
    		@ModelAttribute MyInquiryDto dto) {
        // 본인 글 + status 0 검증
    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
    		dto.setMemberId(memberId);
    		dto.setInquiryId(inquiryId);
    		return ResponseEntity.ok(service.updateMyInquiry(dto));
		} catch (Exception e) {
			log.info("update : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }

    // 문의 삭제
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<?> delete(@PathVariable Long inquiryId) {
        // 본인 글 + status 0 검증
    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
    		return ResponseEntity.ok(service.deleteMyInquiry(inquiryId, memberId));
		} catch (Exception e) {
			log.info("delete : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }

    // 추가문의 등록
    @PostMapping("/{inquiryId}/follow-ups")
    public ResponseEntity<?> createFollowUp(@PathVariable Long inquiryId,
    		@ModelAttribute MyInquiryDto dto) {
    	try {
    		Long memberId = getMemberId();
    		
    		return ResponseEntity.ok(service.insertFollowUp(inquiryId, memberId, dto));
		} catch (Exception e) {
			log.info("createFollowUp : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }
	
    //기존 문의에 파일 업로드
    @PostMapping("/{inquiryId}/files")
    public ResponseEntity<?> fileUpload(@PathVariable Long inquiryId,
    		@RequestParam("file") MultipartFile mf) {
    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
			InquiryFileDto dto = new InquiryFileDto();
			dto.setInquiryId(inquiryId);
    		return ResponseEntity.ok(service.insertInquiryFile(mf, dto, memberId));
		} catch (Exception e) {
			log.info("fileUpload : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }
    
    //첨부파일 목록 조회
    @GetMapping("/{inquiryId}/files")
    public ResponseEntity<?> fileList(@PathVariable Long inquiryId) {

    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
    		return ResponseEntity.ok(service.selectInquiryFiles(inquiryId));
		} catch (Exception e) {
			log.info("fileList : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }
    
    //파일 다운로드
    @GetMapping("/{inquiryId}/files/{fileId}")
    public ResponseEntity<?> fileDownload(@PathVariable Long inquiryId,
    		@PathVariable Long fileId) {
    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
    		return service.fileDownload(inquiryId, fileId);
		} catch (Exception e) {
			log.info("fileDownload : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }
    
    //특정 첨부파일 삭제
    @DeleteMapping("/{inquiryId}/files/{fileId}")
    public ResponseEntity<?> fileDelete(@PathVariable Long inquiryId,
    		@PathVariable Long fileId){
    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
    		return ResponseEntity.ok(service.deleteInquiryFile(fileId, inquiryId, memberId));
		} catch (Exception e) {
			log.info("fileDelete : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }
    
    //특정 문의글의 모든 첨부파일 삭제
    @DeleteMapping("/{inquiryId}/files")
    public ResponseEntity<?> filesDelete(@PathVariable Long inquiryId){
    	
    	try {
    		Long memberId = getMemberId();
			service.selectMyInquiryDetail(inquiryId, memberId);
    		return ResponseEntity.ok(service.deleteInquiryFilesByInquiryId(inquiryId, memberId));
		} catch (Exception e) {
			log.info("fileDelete : ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
    }
	
}
