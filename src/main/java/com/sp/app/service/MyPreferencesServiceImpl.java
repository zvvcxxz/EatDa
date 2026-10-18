package com.sp.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.MyPreferencesDto;
import com.sp.app.mapper.MyPreferencesMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPreferencesServiceImpl implements MyPreferencesService {

    private final MyPreferencesMapper myPreferencesMapper;

    @Override
    @Transactional(readOnly = true)
    public MyPreferencesDto getMemberPreferences(Long memberId) {
        MyPreferencesDto dto = new MyPreferencesDto();
        dto.setMemberId(memberId);
        dto.setCategories(myPreferencesMapper.selectCategoriesWithMemberPref(memberId));
        dto.setTags(myPreferencesMapper.selectTagsWithMemberPref(memberId));
        return dto;
    }

    @Override
    @Transactional //수정사항 반영보다 전체삭제 후 등록이 더 안전하다해서 선택
    public void updateMemberPreferences(Long memberId, MyPreferencesDto dto) {
        // 1. 기존 데이터 전체 삭제
        myPreferencesMapper.deleteMemberCategories(memberId);
        myPreferencesMapper.deleteMemberTags(memberId);

        // 2. 신규 선택 항목 삭제 후 재등록
        List<Long> categoryIds = dto.getCategoryIds();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            myPreferencesMapper.insertMemberCategories(memberId, categoryIds);
        }

        List<Long> tagIds = dto.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            myPreferencesMapper.insertMemberTags(memberId, tagIds);
        }
    }
}