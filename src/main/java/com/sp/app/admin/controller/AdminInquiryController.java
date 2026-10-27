package com.sp.app.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.sp.app.admin.domain.dto.AdminInquiryDto;
import com.sp.app.admin.service.AdminInquiryService;
import com.sp.app.common.PaginateUtil;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/inquiry")
public class AdminInquiryController {
    
    private final AdminInquiryService service;
    private final PaginateUtil paginateUtil;
    
    @GetMapping("/list")
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int current_page,
            @RequestParam(defaultValue = "") String inquiryType,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "all") String schType,
            @RequestParam(defaultValue = "") String kwd,
            Model model) {
        
        int size = 10;
        int offset = (current_page - 1) * size;
        
        Map<String, Object> map = new HashMap<>();
        map.put("inquiryType", inquiryType);
        map.put("status", status);
        map.put("schType", schType);
        map.put("kwd", kwd);
        map.put("offset", offset);
        map.put("size", size);
        
        int dataCount = service.dataCount(map);
        int total_page = paginateUtil.pageCount(dataCount, size);
        if (current_page > total_page) current_page = total_page;
        
        List<AdminInquiryDto> list = service.listInquiry(map);
        
        String listUrl = "/admin/inquiry/list";
        String query = "";
        if (!kwd.isBlank()) {
            query = "schType=" + schType + "&kwd=" + kwd;
        }
        if (!inquiryType.isBlank()) {
            query += (query.isBlank() ? "" : "&") + "inquiryType=" + inquiryType;
        }
        if (!status.isBlank()) {
            query += (query.isBlank() ? "" : "&") + "status=" + status;
        }
        if (!query.isBlank()) {
            listUrl += "?" + query;
        }
        
        String paging = paginateUtil.paging(current_page, total_page, listUrl);
        
        model.addAttribute("list", list);
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("page", current_page);
        model.addAttribute("total_page", total_page);
        model.addAttribute("paging", paging);
        model.addAttribute("inquiryType", inquiryType);
        model.addAttribute("status", status);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        
        return "admin/layout/inquiryList";
    }
    
    @GetMapping("/article")
    public String article(@RequestParam long inquiryId,
                          @RequestParam String page, Model model) {
        AdminInquiryDto dto = service.findById(inquiryId);
        if(dto == null) {
            return "redirect:/admin/inquiry/list?page=" + page;
        }
        
        model.addAttribute("dto", dto);
        model.addAttribute("page", page);
        return "admin/layout/inquiryArticle";
    }
    
    @PostMapping("/answer")
    public String answerSubmit(AdminInquiryDto dto, @RequestParam String page) throws Exception {
        SessionInfo info = LoginMemberUtil.getSessionInfo();
        dto.setAnswerAdminId(info.getMember_id());
        
        service.updateInquiryAnswer(dto);
        
        return "redirect:/admin/inquiry/article?inquiryId=" + dto.getInquiryId() + "&page=" + page;
    }
}