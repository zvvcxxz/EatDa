package com.sp.app.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sp.app.common.FileManager;
import com.sp.app.common.PaginateUtil;
import com.sp.app.domain.dto.CommunityNoticeDto;
import com.sp.app.service.CommunityNoticeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/community/notice")
public class CommunityNoticeController {

    @Autowired
    private CommunityNoticeService service;
    
    @Autowired
    private PaginateUtil paginateUtil;
    
    @Autowired
    private com.sp.app.admin.service.AdminNoticeService noticeFileService;

    @GetMapping({"", "/", "/list"})
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int current_page,
            @RequestParam(value = "schType", defaultValue = "all") String schType,
            @RequestParam(value = "kwd", defaultValue = "") String kwd,
            HttpServletRequest req,
            Model model) {

        int size = 10;
        int offset = (current_page - 1) * size;
        
        Map<String, Object> map = new HashMap<>();
        map.put("offset", offset);
        map.put("size", size);
        map.put("schType", schType);
        map.put("kwd", kwd);

        int dataCount = service.dataCount(map);
        int total_page = paginateUtil.pageCount(dataCount, size);

        current_page = Math.max(1, Math.min(current_page, Math.max(1, total_page)));
        map.put("offset", (current_page - 1) * size);

        List<CommunityNoticeDto> list = service.listNotice(map);

        String cp = req.getContextPath();
        String query = "";
        String listUrl = cp + "/community/notice/list";

        if (!kwd.isBlank()) {
            query = "schType=" + schType + "&kwd=" + java.net.URLEncoder.encode(kwd, java.nio.charset.StandardCharsets.UTF_8);
            listUrl += "?" + query;
        }

        String paging = paginateUtil.paging(current_page, total_page, listUrl);

        model.addAttribute("list", list);
        model.addAttribute("page", current_page);
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("size", size);
        model.addAttribute("total_page", total_page);
        model.addAttribute("paging", paging);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);

        return "community/communityNotice-list";
    }

    @GetMapping("/article")
    public String article(
            @RequestParam("noticeId") long noticeId,
            @RequestParam(value = "page", defaultValue = "1") String page,
            @RequestParam(value = "schType", defaultValue = "all") String schType,
            @RequestParam(value = "kwd", defaultValue = "") String kwd,
            Model model) throws Exception {


        CommunityNoticeDto dto = service.findById(noticeId);
        if (dto == null) {
            return "redirect:/community/notice/list?page=" + page;
        }

        service.updateHitCount(noticeId);
        dto = service.findById(noticeId);
        Map<String, Object> map = new HashMap<>();
        map.put("schType", schType);
        map.put("kwd", kwd);
        map.put("noticeId", noticeId);

        CommunityNoticeDto prevDto = service.findByPrev(map);
        CommunityNoticeDto nextDto = service.findByNext(map);
        List<CommunityNoticeDto> listFile = service.listNoticeFile(noticeId);

        model.addAttribute("dto", dto);
        model.addAttribute("prevDto", prevDto);
        model.addAttribute("nextDto", nextDto);
        model.addAttribute("listFile", listFile);
        model.addAttribute("page", page);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);

        return "community/communityNotice-article";
    }
    
    @GetMapping("/download")
    public void download(@RequestParam("fileId") long fileId, HttpServletResponse response) throws Exception {
        CommunityNoticeDto file = service.findByFileId(fileId);
        if (file == null) { response.sendError(404); return; }
        noticeFileService.downloadFile(file.getFilePath(), file.getOriginalFileName(), response);
    }
}
