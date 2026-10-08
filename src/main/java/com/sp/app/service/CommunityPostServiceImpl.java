package com.sp.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.CommunityPostDto;
import com.sp.app.mapper.CommunityPostMapper;

import lombok.RequiredArgsConstructor;

// Mapper를 호출해서 게시판 기능 처리
@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl implements CommunityPostService {
	// 생성자로 Mapper를 주입받음
	private final CommunityPostMapper mapper;

	@Override
	public void insertPost(CommunityPostDto dto) {
		// 입력한 게시글 저장
		mapper.insertPost(dto);
	}
	
	@Override
	public int dataCount(Map<String, Object> map) {
		// 검색 조건에 맞는 전체 글 수 
		return mapper.dataCount(map);
	}

	@Override
	public List<CommunityPostDto> listPost(Map<String, Object> map) {
		// 현재 페이지에 보여줄 글 목록
		return mapper.listPost(map);
	}

	@Override
	public CommunityPostDto findById(long postId) {
		// 게시글 번호로 상세 내용 조회
		return mapper.findById(postId);
	}

	@Override
	public int updateHitCount(long postId) {
		// 조회수를 올리고, 변경된 행 수를 반환
		return mapper.updateHitCount(postId);
	}

	@Override
	public int updatePost(CommunityPostDto dto) {
		return mapper.updatePost(dto);
	}

	@Override
	public int deletePost(Map<String, Object> map) {
		// 글을 삭제 상태로 변경
		return mapper.deletePost(map);
	}

	@Override
	public int hidePost(long postId) {
		// 글을 숨김 상태로 변경
		return mapper.hidePost(postId);
	}

	@Override
	public CommunityPostDto findByPrev(Map<String, Object> map) {
		return mapper.findByPrev(map);
	}

	@Override
	public CommunityPostDto findByNext(Map<String, Object> map) {
		return mapper.findByNext(map);
	}

	@Override
	public int hasPostLike(Map<String, Object> map) {
		return mapper.hasPostLike(map);
	}

	@Override
	public int postLikeCount(long postId) {
		return mapper.postLikeCount(postId);
	}

	@Override
	public int insertPostLike(Map<String, Object> map) {
		try {
			return mapper.insertPostLike(map);
		} catch (DuplicateKeyException e) {
			// 동시에 요청해도 같은 회원의 좋아요는 한 번만 저장
			return 0;
		}
	}

	@Override
	public int deletePostLike(Map<String, Object> map) {
		return mapper.deletePostLike(map);
	}
	
}
