package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.MemberDto;

@Mapper
public interface MemberMapper {
		//회원 등록 및 SNS 로그인
		public MemberDto loginSnsMember(Map<String, Object> map);
		public void insertMember(MemberDto dto) throws SQLException;
		public void insertSnsMember(MemberDto dto) throws SQLException;

		//회원 정보 및 상태 수정, 탈퇴
		public void updateMember(MemberDto dto) throws SQLException;
		public void updateMemberStatus(Map<String, Object> map) throws SQLException;
		public void withdrawMember(Long member_id) throws SQLException;
		public void updateMemberPassword(MemberDto dto) throws SQLException;
		public void deleteProfilePhoto(Map<String, Object> map) throws SQLException;

		//로그인 시각 및 실패 횟수 관리
		public void updateLastLogin(Long member_id) throws SQLException;
		public void updateLastLoginId(String login_id) throws SQLException;
		
		public int checkFailureCount(String login_id);
		public void updateFailureCountReset(String login_id) throws SQLException;
		public void updateFailureCount(String login_id) throws SQLException;

		//회원 조회
		public MemberDto findById(Long member_id);
		public MemberDto findByLoginId(String login_id);
		public Long getMemberId(String login_id);
		public List<MemberDto> listFindMember(Map<String, Object> map);

		//권한 조회
		public String findAuthorityByLoginId(String login_id);

}
