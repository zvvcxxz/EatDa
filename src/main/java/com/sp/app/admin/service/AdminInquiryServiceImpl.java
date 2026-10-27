package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.sp.app.admin.domain.dto.AdminInquiryDto;
import com.sp.app.admin.mapper.AdminInquiryMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminInquiryServiceImpl implements AdminInquiryService {
    
    private final AdminInquiryMapper mapper;
    
    @Override
    public int dataCount(Map<String, Object> map) {
        return mapper.dataCount(map);
    }

    @Override
    public List<AdminInquiryDto> listInquiry(Map<String, Object> map) {
        return mapper.listInquiry(map);
    }

    @Override
    public AdminInquiryDto findById(long inquiryId) {
        AdminInquiryDto dto = mapper.findById(inquiryId);
        if (dto != null) {
            dto.setFiles(mapper.listInquiryFile(inquiryId));
            
            List<AdminInquiryDto> followUps = mapper.listFollowUp(inquiryId);
            for(AdminInquiryDto child : followUps) {
                child.setFiles(mapper.listInquiryFile(child.getInquiryId()));
            }
            dto.setFollowUps(followUps);
        }
        return dto;
    }

    @Override
    public void updateInquiryAnswer(AdminInquiryDto dto) throws Exception {
        try {
            mapper.updateInquiryAnswer(dto);
        } catch (Exception e) {
            throw e;
        }
    }
}