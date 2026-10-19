package com.sp.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.TogetherShareDto;
import com.sp.app.domain.dto.TogetherShareItemDto;
import com.sp.app.mapper.TogetherShareMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TogetherShareServiceImpl implements TogetherShareService{
	
    private final TogetherShareMapper mapper;
	
	// 재료나눔글 등록
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insertShare(TogetherShareDto dto) throws Exception {
		try {
			if(dto.getAuthor_id() == null) {
				throw new IllegalArgumentException("로그인이 필요합니다.");
			}
			if(dto.getTitle() == null || dto.getTitle().isBlank() 
					|| dto.getContent() == null || dto.getContent().isBlank()) {
				throw new IllegalArgumentException("제목과 내용을 입력해주세요.");
			}
			if(dto.getLocation_text() == null || dto.getLocation_text().isBlank()) {
				throw new IllegalArgumentException("나눔 장소를 입력해주세요.");
			}
			
			if(dto.getMax_recipients() == null || dto.getMax_recipients() < 1) {
				throw new IllegalArgumentException("최대 수령 인원은 1명 이상이어야 합니다.");
			}
			
			// 나눔 재료 확인
			if(dto.getItems() == null || dto.getItems().isEmpty()) {
				throw new IllegalArgumentException("나눔할 재료를 추가해주세요.");
			}
			
			for(TogetherShareItemDto item : dto.getItems()) {
				if(item == null || item.getIngredient_id() == null) {
					throw new IllegalArgumentException("재료를 선택해주세요.");
				}
				
				if(item.getQuantity() == null ||
						item.getQuantity().signum() <= 0) { 
					throw new IllegalArgumentException("재료 수량은 0보다 커야 합니다.");
				}
				
				if(item.getUnit() == null || item.getUnit().isBlank()) {
					throw new IllegalArgumentException("재료 단위를 입력해주세요.");
				}
			}
			
			int count = mapper.insertShare(dto);
			
			if(count != 1) {
				throw new IllegalStateException("게시글을 등록하지 못했습니다.");
			}
			
			// 게시글 번호를 넣어서 재료도 저장
			for(TogetherShareItemDto item : dto.getItems()) {
				item.setShare_id(dto.getShare_id());
				
				int itemCount = mapper.insertShareItem(item);
				
				if(itemCount != 1) {
					throw new IllegalStateException("나눔 재료를 등록하지 못했습니다.");
				}
			}
			
			return count;
		} catch (Exception e) {
			log.error("재료나눔 글 등록 실패", e);
			throw e;
		}
	}

	// 재료명 / 별칭 검색
	@Override
	@Transactional(readOnly = true)
	public List<TogetherShareItemDto> searchIngredients(String keyword) throws Exception {
		
		try {
			if(keyword == null || keyword.isBlank()) {
				return List.of();
			}
			
			return mapper.searchIngredients(keyword.trim());
			
		} catch (Exception e) {
			log.error("재료나눔 재료 검색 실패" , e);
			throw e;
		}
	}

	
	// 분류별 재료 목록
	@Override
	@Transactional(readOnly = true)
	public List<TogetherShareItemDto> listIngredientsByType(String type) throws Exception {
		
		try {
			if(type == null || type.isBlank()) {
				return List.of();
			}
			
			return mapper.listIngredientsByType(type.trim());
			
		} catch (Exception e) {
			log.error("재료나눔 분류별 조회 실패" , e);
			throw e;
		}
	}
}
