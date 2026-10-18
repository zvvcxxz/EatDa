package com.sp.app.service;

import com.sp.app.domain.dto.MyPreferencesDto;

public interface MyPreferencesService {
    MyPreferencesDto getMemberPreferences(Long memberId);
    void updateMemberPreferences(Long memberId, MyPreferencesDto dto);
}