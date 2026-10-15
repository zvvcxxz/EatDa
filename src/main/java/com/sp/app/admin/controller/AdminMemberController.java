package com.sp.app.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.app.admin.domain.dto.AdminMemberDto;
import com.sp.app.admin.service.AdminMemberService;
import com.sp.app.common.MyUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/member")
@Slf4j
public class AdminMemberController {

    private final AdminMemberService service;
    private final MyUtil myUtil;

    @GetMapping(value = {"", "/", "main"})
    public String main() {
        return "admin/layout/memberMain"; 
    }

    @GetMapping("list")
    public String list(
            @RequestParam(name = "page", defaultValue = "1") int current_page,
            @RequestParam(name = "schType", defaultValue = "loginId") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "roleId", defaultValue = "") String roleId,
            Model model) {

        try {
            int size = 10;
            Map<String, Object> map = new HashMap<>();
            map.put("schType", schType);
            map.put("kwd", kwd);
            map.put("status", status);
            map.put("roleId", roleId);

            int dataCount = service.dataCount(map);
            int total_page = (dataCount / size) + (dataCount % size > 0 ? 1 : 0);

            if (current_page > total_page) {
                current_page = total_page;
            }

            int offset = (current_page - 1) * size;
            if(offset < 0) offset = 0;

            map.put("offset", offset);
            map.put("size", size);

            List<AdminMemberDto> list = service.listMember(map);

            model.addAttribute("list", list);
            model.addAttribute("page", current_page);
            model.addAttribute("dataCount", dataCount);
            model.addAttribute("size", size);
            model.addAttribute("total_page", total_page);
            
            model.addAttribute("schType", schType);
            model.addAttribute("kwd", kwd);
            model.addAttribute("status", status);
            model.addAttribute("roleId", roleId);

        } catch (Exception e) {
            log.error("list error", e);
        }

        return "admin/layout/memberList"; 
    }

    @GetMapping("profile")
    public String profile(@RequestParam(name="memberId") Long memberId,
                          @RequestParam(name="page", defaultValue = "1") int page,
                          Model model) {
        
        try {
            AdminMemberDto dto = service.findById(memberId);
            List<AdminMemberDto> listStatus = service.listMemberStatusHist(memberId);
            
            model.addAttribute("dto", dto);
            model.addAttribute("listStatus", listStatus);
            model.addAttribute("page", page);
        } catch (Exception e) {
            log.error("profile error", e);
        }

        return "admin/layout/memberProfile"; 
    }
    
    @ResponseBody
    @PutMapping("update")
    public Map<String, Object> updateMember(AdminMemberDto dto) {
        Map<String, Object> model = new HashMap<>();
        
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("memberId", dto.getMemberId());
            paramMap.put("name", dto.getName());
            paramMap.put("birth", dto.getBirth());
            paramMap.put("roleId", dto.getRoleId());
            
            service.updateMember(paramMap); 
            
            model.put("state", "true");
        } catch (Exception e) {
            log.error("update error", e);
            model.put("state", "false");
        }
        
        return model;
    }

    @ResponseBody
    @PostMapping("status")
    public Map<String, Object> updateMemberStatus(AdminMemberDto dto) {
        Map<String, Object> model = new HashMap<>();
        
        try {
            dto.setAdminId(1L);

            service.updateMemberStatus(dto); 
            model.put("state", "true");
        } catch (Exception e) {
            log.error("status update error", e);
            model.put("state", "false");
        }
        
        return model;
    }
}