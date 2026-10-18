package com.sp.app.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.sp.app.domain.dto.MyPreferencesDto.CategoryItem;
import com.sp.app.domain.dto.MyPreferencesDto.TagItem;

@Mapper
public interface MyPreferencesMapper {

    // 1. 회원의 선택 상태가 포함된 전체 카테고리/태그 목록 조회
    List<CategoryItem> selectCategoriesWithMemberPref(@Param("memberId") Long memberId);
    List<TagItem> selectTagsWithMemberPref(@Param("memberId") Long memberId);

    // 2. 기존 선호 정보 삭제
    void deleteMemberCategories(@Param("memberId") Long memberId);
    void deleteMemberTags(@Param("memberId") Long memberId);

    // 3. 신규 선호 정보 일괄 등록
    void insertMemberCategories(@Param("memberId") Long memberId, @Param("categoryIds") List<Long> categoryIds);
    void insertMemberTags(@Param("memberId") Long memberId, @Param("tagIds") List<Long> tagIds);
}