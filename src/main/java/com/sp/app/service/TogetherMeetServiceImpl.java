package com.sp.app.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.TogetherMeetDto;
import com.sp.app.domain.dto.TogetherMeetParticipantDto;
import com.sp.app.mapper.TogetherMeetMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TogetherMeetServiceImpl implements TogetherMeetService {

	private final TogetherMeetMapper mapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertMeet(TogetherMeetDto dto) throws Exception {

		try {
			mapper.insertMeet(dto);
			log.info("같이 먹어요 글 등록 성공");
		} catch (Exception e) {
			log.error("같이 먹어요 글 등록 실패", e);
			throw e;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<TogetherMeetDto> listMeet() throws Exception {

		try {
			List<TogetherMeetDto> list = mapper.listMeet();

			log.info("같이 먹어요 글 목록 조회 완료 : {} 건", list.size());

			return list;

		} catch (Exception e) {
			log.error("같이 먹어요 글 목록 조회 실패", e);
			throw e;
		}

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TogetherMeetDto findMeetById(Long meet_id) throws Exception {

		try {

			mapper.updateViewCount(meet_id);

			return mapper.findMeetById(meet_id);

		} catch (Exception e) {
			log.error("같이 먹어요 글 상세보기 실패", e);
			throw e;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public int countMeetParticipants(Long meet_id) throws Exception {

		try {
			return mapper.countMeetParticipants(meet_id);
		} catch (Exception e) {
			log.error("같이 먹어요 참여 인원 조회 실패", e);
			throw e;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int createMeetParticipants(Long meet_id, Long member_id) throws Exception {

		try {
			TogetherMeetDto dto = mapper.findMeetById(meet_id);
			
			if(dto == null) {
				throw new IllegalArgumentException("게시글이 없습니다.");
			}
			
			if(Objects.equals(dto.getAuthor_id(), member_id)) {
				throw new IllegalStateException(
						"작성자는 이미 참여 인원에 포함되어 있습니다.");
			}
			
	        if (mapper.countMeetApplication(meet_id, member_id) > 0) {
	            throw new IllegalStateException(
	                    "이미 참여 신청한 모임입니다. 참여 신청은 한 번만 가능합니다.");
	        }
			int count = mapper.createMeetParticipant(meet_id, member_id);
			
			if(count != 1) {
				throw new IllegalStateException("참여 신청을 저장하지 못했습니다.");
			}
	        
			return count;
		} catch (Exception e) {
			log.error("같이먹어요 참여 신청 실패", e);
			throw e;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<TogetherMeetParticipantDto> listParticipant(Long meet_id) throws Exception {

		try {
			return mapper.listParticipant(meet_id);
		} catch (Exception e) {
			log.error("같이먹어요 신청자 목록 조회 실패" , e);
			throw e;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int approveMeetParticipant(Long meet_id, Long member_id, Long author_id) throws Exception {
		
		try {
			TogetherMeetDto dto = mapper.findMeetById(meet_id);
			
			if(dto == null) {
				throw new IllegalArgumentException("게시글이 없습니다.");
			}
			
			if(author_id == null || !Objects.equals(dto.getAuthor_id(), author_id)) {
				throw new IllegalStateException("글 작성자만 승인할 수 있습니다.");
			}
			
			int count = mapper.approveMeetParticipant(meet_id, member_id);
			
			if(count != 1) {
				throw new IllegalStateException(
						"대기 중인 신청이 없거나 이미 처리된 신청입니다.");
			}
			
			return count;
			
		} catch (Exception e) {
			log.error("같이 먹어요 참여 신청 승인 오류", e);
			throw e;
		}
}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int rejectMeetParticipant(Long meet_id, Long member_id, Long author_id) throws Exception {
			
		try {
			TogetherMeetDto dto = mapper.findMeetById(meet_id);
			
			if(dto == null) {
				throw new IllegalArgumentException("게시글이 없습니다.");
			}
			
			if(author_id == null || !Objects.equals(dto.getAuthor_id(), author_id)) {
				throw new IllegalStateException("글 작성자만 거절할 수 있습니다.");
			}
			
			int count = mapper.rejectMeetParticipant(meet_id, member_id);
			
			if (count != 1) {
			    throw new IllegalStateException(
			            "대기 중인 신청이 없거나 이미 처리된 신청입니다.");
			}
			
			return count;
		} catch (Exception e) {
			log.error("같이 먹어요 참여 신청 거절 오류", e);
			throw e;
		}
	}
}