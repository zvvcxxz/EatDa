package com.sp.app.service;

import java.util.ArrayList;
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
		        throw new IllegalArgumentException("문의를 찾을 수 없습니다.");
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
		List<String> uploadedPaths = new ArrayList<>();
		try {
			result = mapper.insertMyInquiry(dto);
			if (result == 0) 
				throw new IllegalStateException("문의글 등록에 실패하였습니다.");			
			
			// 파일 저장 및 DB 등록 (예외 발생 시 디스크 cleanup 보장)
	         if(!CollectionUtils.isEmpty(dto.getUploadFiles())) {
	            for(MultipartFile mf : dto.getUploadFiles()) {
	               if (mf == null || mf.isEmpty()) continue;
	               
	               String saveFilename = Objects.requireNonNull(
	                     storageService.uploadFileToServer(mf, uploadPath));
	               uploadedPaths.add(saveFilename);

	               InquiryFileDto filedto = new InquiryFileDto();
	               filedto.setFilePath(saveFilename);
	               filedto.setOriginalFileName(mf.getOriginalFilename());
	               filedto.setInquiryId(dto.getInquiryId());
	               int fileResult = mapper.insertInquiryFile(filedto);  
	               if(fileResult==0) {
	            	   throw new IllegalStateException("파일 등록에 실패했습니다.");
	               }
	            }
	         }
	      } catch (Exception e) {
	         // 트랜잭션 롤백 시 저장된 물리 파일 즉시 삭제
	         for (String path : uploadedPaths) {
	            deleteUploadFile(path);
	         }
	         log.info("insertMyInquiry error: ", e);
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
	@Transactional	//근데 지금은 최상위 부모만 자식가능하게 설정해서 재귀없어도 됨
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
	    deleteInquiryFilesByInquiryId(inquiryId, memberId);
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
	
	@Override //추가문의등록
	@Transactional
	public int insertFollowUp(Long parentInquiryId, Long memberId, MyInquiryDto dto) {
		try {
			MyInquiryDto parent = mapper.selectTopLevelInquiry(parentInquiryId, memberId);
			if(parent == null) {
				throw new IllegalArgumentException("답변이 완료된 최상위 문의에만 추가 문의를 할 수 있습니다.");
			}
			
			dto.setMemberId(memberId);
			dto.setParentInquiryId(parentInquiryId);
			if (dto.getInquiryType() == null) {
	             dto.setInquiryType(parent.getInquiryType());
	         }
			return insertMyInquiry(dto);	
			
		} catch (Exception e) {
			log.info("insertFollowUp : ", e);
			throw e;
		}
	}

	@Override// 첨부파일 등록
	@Transactional
	public int insertInquiryFile(MultipartFile mf, InquiryFileDto dto, Long memberId) {
		int result = 0;
		String saveFilename = null;
		try {
			if(mapper.selectInquiryByStatus(dto.getInquiryId(), memberId) ==0) {
				throw new IllegalStateException("답변이 완료된 문의에는 파일을 추가하거나 삭제할 수 없습니다.");
			}
			
			if(mf == null || mf.isEmpty()) {
				throw new IllegalArgumentException("업로드할 파일이 없습니다.");
			}
			saveFilename = Objects.requireNonNull(
					storageService.uploadFileToServer(mf, uploadPath));
			
			dto.setFilePath(saveFilename);
			dto.setOriginalFileName(mf.getOriginalFilename());
			
			result = mapper.insertInquiryFile(dto);
			
			if(result == 0) {
				throw new IllegalStateException("파일 등록에 실패했습니다.");
			}
		} catch (Exception e) {
			if(saveFilename !=null) {
				deleteUploadFile(saveFilename);
			}
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
	public int deleteInquiryFilesByInquiryId(Long inquiryId, Long memberId) {
		int result = 0;
		try {
			if(mapper.selectInquiryByStatus(inquiryId, memberId) ==0) {
				throw new IllegalStateException("답변이 완료된 문의에는 파일을 추가하거나 삭제할 수 없습니다.");
			}
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
	public int deleteInquiryFile(Long inquiryFileId, Long inquiryId, Long memberId) {
		int result = 0;
		try {
			if(mapper.selectInquiryByStatus(inquiryId, memberId) ==0) {
				throw new IllegalStateException("답변이 완료된 문의에는 파일을 추가하거나 삭제할 수 없습니다.");
			}
			InquiryFileDto dto = mapper.selectInquiryFile(inquiryFileId, inquiryId);
			if (dto == null) {
			    throw new IllegalArgumentException("첨부파일을 찾을 수 없습니다.");
			}
			result = mapper.deleteInquiryFile(inquiryFileId, inquiryId);
			deleteUploadFile(dto.getFilePath());
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
	         } else {
	        	 throw new IllegalArgumentException("추가 문의 목록을 조회할 수 없습니다.");
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
			if (result == 0) {
	            throw new IllegalStateException("관리자 답변이 완료되어 수정/삭제할 수 없거나 존재하지 않는 문의입니다.");
	         }
			return result;
		} catch (Exception e) {
			log.info("selectInquiryByStatus : ", e);
			throw e;
		}
	}


}
