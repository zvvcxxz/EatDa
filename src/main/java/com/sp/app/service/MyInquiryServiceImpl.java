package com.sp.app.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.common.MyUtil;
import com.sp.app.common.StorageService;
import com.sp.app.domain.dto.MyInquiryDto;
import com.sp.app.domain.dto.MyInquiryDto.InquiryFileDto;
import com.sp.app.exception.StorageException;
import com.sp.app.mapper.MyInquiryMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MyInquiryServiceImpl implements MyInquiryService {
	
	private final MyInquiryMapper mapper;
	private final StorageService storageService;	
	private final MyUtil myUtil;
	
	@Value("${file.upload-root}/inquiry")
	private String uploadPath;

	@Override// 내 문의 목록: 검색 + 유형 필터 + 페이징
	public List<MyInquiryDto> selectMyInquiryList(Map<String, Object> map) {
		List<MyInquiryDto> list = null;
		
		try {
			list = mapper.selectMyInquiryList(map);
			for(MyInquiryDto dto : list) {
				dto.setTitle(myUtil.htmlSymbols(dto.getTitle()));
				dto.setContent(myUtil.sanitize(dto.getContent()));
			}
		} catch (Exception e) {
			log.info("selectMyInquiryList : ", e);
			throw e;
		}

		return list;
	}

	@Override// 검색 조건에 맞는 전체 문의 개수
	public long countMyInquiryList(Map<String, Object> map) {
		long count = 0L;
		
		try {
			count = mapper.countMyInquiryList(map);
		} catch (Exception e) {
			log.info("countMyInquiryList : ", e);
			throw e;
		}

		return count;
	}

	@Override// 내 문의 상세
	public MyInquiryDto selectMyInquiryDetail(Long inquiryId, Long memberId) {
		MyInquiryDto dto = null;
		
		try {
			dto = mapper.selectMyInquiryDetail(inquiryId, memberId);
			if (dto == null) {
		        throw new IllegalArgumentException("권한이 없습니다.");
		    }
			dto.setFiles(mapper.selectInquiryFiles(inquiryId)); 
		} catch (Exception e) {
			log.info("selectMyInquiryList : ", e);
			throw e;
		}
	    return dto;
	}

	@Override// 문의 등록  
	@Transactional
	public int insertMyInquiry(MyInquiryDto dto) {
		int result = 0;
		try {
			result = mapper.insertMyInquiry(dto);
			if (result == 0) return 0;
			
			if(!CollectionUtils.isEmpty(dto.getUploadFiles())) {
				for(MultipartFile mf : dto.getUploadFiles()) {
					String saveFilename = Objects.requireNonNull(
							storageService.uploadFileToServer(mf, uploadPath));
					InquiryFileDto filedto = new InquiryFileDto();
					filedto.setFilePath(saveFilename);
					filedto.setOriginalFileName(mf.getOriginalFilename());
					filedto.setInquiryId(dto.getInquiryId());
					mapper.insertInquiryFile(filedto);					
				}
			}
			
		} catch (Exception e) {
			log.info("insertMyInquiry : ", e);
			throw e;
		}
		return result;
	}

	@Override// 문의 수정
	@Transactional
	public int updateMyInquiry(MyInquiryDto dto) {
		int result = 0;
		try {
			if(mapper.selectInquiryByStatus(dto.getInquiryId(), dto.getMemberId())==0) return 0;
			result = mapper.updateMyInquiry(dto);
		} catch (Exception e) {
			log.info("updateMyInquiry : ", e);
			throw e;
		}
		return result;
	}

	@Override// 문의 삭제
	@Transactional
	public int deleteMyInquiry(Long inquiryId, Long memberId) {
		int result = 0;
		try {
			if(mapper.selectInquiryByStatus(inquiryId, memberId)==0) return 0;
			
			List<InquiryFileDto> files = mapper.selectInquiryFiles(inquiryId);
			
			result = mapper.deleteMyInquiry(inquiryId, memberId);
			if (result == 0) {
				throw new IllegalStateException("문의 삭제에 실패했습니다.");
			}
			
			if(!CollectionUtils.isEmpty(files)) {
				deleteInquiryFilesByInquiryId(inquiryId);
			}
			
		} catch (Exception e) {
			log.info("deleteMyInquiry : ", e);
			throw e;
		}
		return result;
	}

	@Override // 추가 문의 목록
	public List<MyInquiryDto> selectFollowUpInquiries(Long parentInquiryId, Long memberId) {
		List<MyInquiryDto> list = null;
		
		try {
			list = mapper.selectFollowUpInquiries(parentInquiryId, memberId);
		} catch (Exception e) {
			log.info("selectFollowUpInquiries : ", e);
			throw e;
		}

		return list;
	}

	@Override// 첨부파일 등록
	@Transactional
	public int insertInquiryFile(MultipartFile mf, InquiryFileDto dto) {
		int result = 0;
		try {
			String saveFilename = Objects.requireNonNull(
					storageService.uploadFileToServer(mf, uploadPath));
			
			dto.setFilePath(saveFilename);
			dto.setOriginalFileName(mf.getOriginalFilename());
			dto.setInquiryId(dto.getInquiryId());
			
			result = mapper.insertInquiryFile(dto);
			
		} catch (Exception e) {
			log.info("insertInquiryFile : ", e);
			throw e;
		}
		return result;
	}
	

	@Override// 문의의 첨부파일 목록
	public List<InquiryFileDto> selectInquiryFiles(Long inquiryId) {
		List<InquiryFileDto> list = null;
		
		try {
			list = mapper.selectInquiryFiles(inquiryId);
		} catch (Exception e) {
			log.info("selectInquiryFiles : ", e);
			throw e;
		}

		return list;
	}

	@Override// 특정 첨부파일 조회
	public InquiryFileDto selectInquiryFile(Long inquiryFileId, Long inquiryId) {
		InquiryFileDto dto = null;
		
		try {
			dto = mapper.selectInquiryFile(inquiryFileId, inquiryId);
		} catch (Exception e) {
			log.info("selectInquiryFile : ", e);
			throw e;
		}

		return dto;
	}

	@Override// 문의에 연결된 첨부파일 DB 정보 전체 삭제
	@Transactional
	public int deleteInquiryFilesByInquiryId(Long inquiryId) {
		int result = 0;
		try {
			List<InquiryFileDto> list = mapper.selectInquiryFiles(inquiryId);
			for(InquiryFileDto dto : list) {
				deleteUploadFile(dto.getFilePath());
			}
			result = mapper.deleteInquiryFilesByInquiryId(inquiryId);
		} catch (Exception e) {
			log.info("deleteInquiryFilesByInquiryId : ", e);
			throw e;
		}
		return result;
	}

	@Override// 특정 첨부파일 DB 정보 삭제
	@Transactional
	public int deleteInquiryFile(Long inquiryFileId, Long inquiryId) {
		int result = 0;
		try {
			InquiryFileDto dto = mapper.selectInquiryFile(inquiryFileId, inquiryId);
			deleteUploadFile(dto.getFilePath());
			result = mapper.deleteInquiryFile(inquiryFileId, inquiryId);
		} catch (Exception e) {
			log.info("deleteInquiryFile : ", e);
			throw e;
		}
		return result;
	}
	
	public boolean deleteUploadFile(String filename) {
		return storageService.deleteFile(uploadPath, filename);
	}

	@Override //추가문의목록 상세
	public MyInquiryDto selectFollowUpDetail(Long inquiryId, Long followUpId, Long memberId) {
		MyInquiryDto dto = null;
		
		try {
			dto = mapper.selectFollowUpDetail(inquiryId, followUpId, memberId);
		} catch (Exception e) {
			log.info("selectFollowUpDetail : ", e);
			throw e;
		}

		return dto;
	}

	@Override
	public ResponseEntity<?> fileDownload(Long inquiryId, Long fileId) {
		try {
			InquiryFileDto dto = mapper.selectInquiryFile(fileId, inquiryId);
			
			return storageService.downloadFile(uploadPath, dto.getFilePath(), dto.getOriginalFileName());
		} catch (NullPointerException | StorageException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
		
	}

	@Override
	public int selectInquiryByStatus(Long inquiryId, Long memberId) {
		int result = 0;
		
		try {
			result = mapper.selectInquiryByStatus(inquiryId, memberId);
			if (result == 1) {
				throw new IllegalStateException("추가 문의 등록이 아직 불가합니다.");
			}
			return result;
		} catch (Exception e) {
			log.info("selectInquiryByStatus : ", e);
			throw e;
		}
	}

}
