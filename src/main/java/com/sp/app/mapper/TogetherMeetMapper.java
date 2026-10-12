package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sp.app.domain.dto.TogetherMeetDto;
import com.sp.app.domain.dto.TogetherMeetParticipantDto;

@Mapper
//커뮤니티 - 같이먹어요
public interface TogetherMeetMapper {
	public void insertMeet(TogetherMeetDto dto) throws SQLException; // 같이먹어요 글 작성

	public List<TogetherMeetDto> listMeet(); // 같이먹어요 글 목록(리스트)

	public TogetherMeetDto findMeetById(Long meet_id); // 같이 먹어요 글 상세보기

	public void updateViewCount(@Param("meet_id") Long meet_id); // 글 상세보기 - 조회수 증가

	public int countMeetParticipants(@Param("meet_id") Long meet_id); // 글 상세보기 - 참여 현황

	public int createMeetParticipant(@Param("meet_id") Long meet_id, @Param("member_id") Long member_id); // 같이먹어요 참여 신청
	
	public List<TogetherMeetParticipantDto> listParticipant(@Param("meet_id") Long meet_id); // 같이먹어요 신청자 목록
	
	public int approveMeetParticipant(@Param("meet_id") Long meet_id, @Param("member_id") Long member_id); // 참여 신청 승인
	
	public int rejectMeetParticipant(@Param("meet_id") Long meet_id, @Param("member_id") Long member_id); // 참여 신청 거절
	
	public int countMeetApplication(@Param("meet_id") Long meet_id, @Param("member_id") Long member_id);
}
