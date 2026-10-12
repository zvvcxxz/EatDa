package com.sp.app.admin.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sp.app.admin.domain.dto.AdminNoticeDto;
import com.sp.app.admin.service.AdminNoticeService;

@Controller
@RequestMapping("/admin/notice")
public class AdminNoticeController {

    @Autowired
    private AdminNoticeService service;

    private String getPathName(HttpSession session) {
        String root = session.getServletContext().getRealPath("/");
        return root + "uploads" + File.separator + "notice";
    }

    @GetMapping({"", "/list"})
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int current_page,
            HttpServletRequest req,
            Model model) {
        
        int size = 10;
        int offset = (current_page - 1) * size;
        
        Map<String, Object> map = new HashMap<>();
        map.put("offset", offset);
        map.put("size", size);

        List<AdminNoticeDto> topList = service.listNoticeTop();
        List<AdminNoticeDto> list = service.listNotice(map);
        
        int dataCount = service.dataCount(map);
        int total_page = (int) Math.ceil((double) dataCount / size);

        model.addAttribute("topList", topList);
        model.addAttribute("list", list);
        model.addAttribute("page", current_page);
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("total_page", total_page);

        return "admin/layout/noticeList"; 
    }

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("mode", "write");
        return "admin/layout/noticeWrite"; 
    }

    @PostMapping("/write")
    public String writeSubmit(AdminNoticeDto dto, HttpSession session) {
        try {
            dto.setAdminId(1L); 
            
            String pathname = getPathName(session);
            service.insertNotice(dto, pathname);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/notice";
    }

    @GetMapping("/article")
    public String article(@RequestParam long noticeId, Model model) {
        try {
            service.updateHitCount(noticeId);
            AdminNoticeDto dto = service.findById(noticeId);
            
            if (dto == null) {
                return "redirect:/admin/notice";
            }
            
            List<AdminNoticeDto> listFile = service.listNoticeFile(noticeId);
            
            model.addAttribute("dto", dto);
            model.addAttribute("listFile", listFile);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/layout/noticeArticle";
    }

    @GetMapping("/update")
    public String updateForm(@RequestParam long noticeId, Model model) {
        AdminNoticeDto dto = service.findById(noticeId);
        if (dto == null) {
            return "redirect:/admin/notice";
        }
        
        List<AdminNoticeDto> listFile = service.listNoticeFile(noticeId);
        
        model.addAttribute("mode", "update");
        model.addAttribute("dto", dto);
        model.addAttribute("listFile", listFile);
        
        return "admin/main/noticeWrite";
    }

    @PostMapping("/update")
    public String updateSubmit(AdminNoticeDto dto, HttpSession session) {
        try {
            String pathname = getPathName(session);
            service.updateNotice(dto, pathname);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/notice/article?noticeId=" + dto.getNoticeId();
    }

    @GetMapping("/delete")
    public String delete(@RequestParam long noticeId, HttpSession session) {
        try {
            String pathname = getPathName(session);
            service.deleteNotice(noticeId, pathname);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/notice";
    }
}