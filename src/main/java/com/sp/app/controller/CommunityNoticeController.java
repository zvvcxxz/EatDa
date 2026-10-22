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
    private FileManager fileManager;

    @GetMapping({"/list"})
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

        if (current_page > total_page) {
            current_page = total_page;
        }

        List<CommunityNoticeDto> list = service.listNotice(map);

        String cp = req.getContextPath();
        String query = "";
        String listUrl = cp + "/community/notice/list";

        if (!kwd.isBlank()) {
            query = "schType=" + schType + "&kwd=" + kwd;
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
            @RequestParam long noticeId,
            @RequestParam(value = "page", defaultValue = "1") String page,
            @RequestParam(value = "schType", defaultValue = "all") String schType,
            @RequestParam(value = "kwd", defaultValue = "") String kwd,
            Model model) throws Exception {

        service.updateHitCount(noticeId);

        CommunityNoticeDto dto = service.findById(noticeId);
        if (dto == null) {
            return "redirect:/community/notice/list?page=" + page;
        }

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
    public void download(@RequestParam long fileId,
                         HttpServletRequest req,
                         HttpServletResponse resp,
                         HttpSession session) throws Exception {
        
        CommunityNoticeDto dto = service.findByFileId(fileId);
        
        if(dto != null) {
            String root = session.getServletContext().getRealPath("/");
            String pathname = root + "uploads" + File.separator + "notice" + File.separator + dto.getFilePath();
            
            File file = new File(pathname);
            
            if(file.exists()) {
                String originalFilename = dto.getOriginalFileName();
                
                String browser = req.getHeader("User-Agent");
                if (browser.contains("MSIE") || browser.contains("Trident") || browser.contains("Edge")) {
                    originalFilename = java.net.URLEncoder.encode(originalFilename, "UTF-8").replaceAll("\\+", "%20");
                } else {
                    originalFilename = new String(originalFilename.getBytes("UTF-8"), "ISO-8859-1");
                }
                
                resp.setContentType("application/octet-stream");
                resp.setHeader("Content-disposition", "attachment; filename=\"" + originalFilename + "\"");
                resp.setContentLength((int) file.length());
                
                try (java.io.BufferedInputStream bis = new java.io.BufferedInputStream(new java.io.FileInputStream(file));
                     java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(resp.getOutputStream())) {
                    
                    byte[] b = new byte[8192];
                    int len;
                    while ((len = bis.read(b)) != -1) {
                        bos.write(b, 0, len);
                    }
                    bos.flush();
                    return;
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        resp.setContentType("text/html; charset=utf-8");
        resp.getWriter().print("<script>alert('파일이 존재하지 않거나 다운로드에 실패했습니다.');history.back();</script>");
    }
}