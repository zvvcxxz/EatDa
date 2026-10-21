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
import com.sp.app.exception.StorageFileNotFoundException;
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
			log.info("selectMyInquiryDetail : ", e);
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
					if (mf == null || mf.isEmpty()) continue;
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
			if(mapper.selectInquiryByStatus(dto.getInquiryId(), dto.getMemberId())==0) 
				throw new IllegalStateException("답변이 완료된 문의는 수정할 수 없습니다.");
			
			result = mapper.updateMyInquiry(dto);

		} catch (Exception e) {
			log.info("updateMyInquiry : ", e);
			throw e;
		}
		return result;
	}

	@Override// 문의 삭제 (고아 자료 방지를 위해 재귀순회)
	@Transactional
	public int deleteMyInquiry(Long inquiryId, Long memberId) {

		try {
			if(mapper.selectInquiryByStatus(inquiryId, memberId)==0) 
			throw new IllegalStateException("답변이 완료된 문의는 삭제할 수 없습니다.");
			
			deleteInquiryCascade(inquiryId, memberId);

	        return 1;
			
		} catch (Exception e) {
			log.info("deleteMyInquiry : ", e);
			throw e;
		}
	}
	private void deleteInquiryCascade(Long inquiryId, Long memberId) {
	    //하위 추가 문의 목록 조회
	    List<MyInquiryDto> followUps = mapper.selectFollowUpInquiries(inquiryId, memberId);

	    if (!CollectionUtils.isEmpty(followUps)) {
	        for (MyInquiryDto child : followUps) {
	            deleteInquiryCascade(child.getInquiryId(), memberId);
	        }
	    }
	    deleteInquiryFilesByInquiryId(inquiryId);
	    mapper.deleteMyInquiry(inquiryId, memberId);
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
			if(mf == null || mf.isEmpty()) {
				throw new IllegalArgumentException("업로드할 파일이 없습니다.");
			}
			String saveFilename = Objects.requireNonNull(
					storageService.uploadFileToServer(mf, uploadPath));
			
			dto.setFilePath(saveFilename);
			dto.setOriginalFileName(mf.getOriginalFilename());
			
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
			result = mapper.deleteInquiryFilesByInquiryId(inquiryId);
			
			for(InquiryFileDto dto : list) {
				deleteUploadFile(dto.getFilePath());
			}
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
			if (dto == null) {
			    throw new IllegalArgumentException("첨부파일을 찾을 수 없습니다.");
			}
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
			if (dto != null) {
	            dto.setFiles(mapper.selectInquiryFiles(followUpId));
	         }
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
			if (dto == null) {
		        throw new StorageFileNotFoundException("해당 첨부파일을 찾을 수 없습니다.");
		    }
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
			selectMyInquiryDetail(inquiryId, memberId);
			result = mapper.selectInquiryByStatus(inquiryId, memberId);
			if (result == 1) {
				throw new IllegalStateException("관리자 답변이 완료되지 않아 추가 문의 등록이 아직 불가합니다.");
			}
			return result;
		} catch (Exception e) {
			log.info("selectInquiryByStatus : ", e);
			throw e;
		}
	}

}
