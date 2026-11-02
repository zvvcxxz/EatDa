package com.sp.app.admin.controller;

import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.admin.domain.dto.AdminNoticeDto;
import com.sp.app.admin.service.AdminNoticeService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin/notice")
public class AdminNoticeController {
    private static final Logger log = LoggerFactory.getLogger(AdminNoticeController.class);
    private final AdminNoticeService service;
    public AdminNoticeController(AdminNoticeService service) {
        this.service = service;
    }
    @GetMapping({"", "/", "/list"})
    public String list(@RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="schType", defaultValue="all") String schType,
            @RequestParam(value="kwd", defaultValue="") String kwd, Model model) {
        Map<String,Object> map = new HashMap<>();
        map.put("schType", schType); map.put("kwd", kwd);
        int count = service.dataCount(map), size = 10;
        int pages = (count + size - 1) / size;
        page = Math.max(1, Math.min(page, Math.max(1, pages)));
        map.put("offset", (page - 1) * size); map.put("size", size);
        model.addAttribute("list", service.listNotice(map));
        model.addAttribute("topList", service.listNoticeTop());
        model.addAttribute("page", page); model.addAttribute("size", size);
        model.addAttribute("total_page", pages); model.addAttribute("dataCount", count);
        model.addAttribute("schType", schType); model.addAttribute("kwd", kwd);
        return "admin/layout/noticeList";
    }
    @GetMapping("/write")
    public String writeForm(Model model) {
        AdminNoticeDto dto = new AdminNoticeDto(); dto.setStatus(1);
        model.addAttribute("dto", dto); model.addAttribute("mode", "write");
        return "admin/layout/noticeWrite";
    }
    @PostMapping("/write")
    public String write(AdminNoticeDto dto, @AuthenticationPrincipal CustomUserDetails user,
                        Model model, RedirectAttributes flash) {
        if (user == null) return "redirect:/member/login";
        try {
            dto.setAdminId(user.getMember().getMember_id());
            service.insertNotice(dto, null);
            flash.addFlashAttribute("message", "공지사항이 등록되었습니다.");
            return "redirect:/admin/notice/list";
        } catch (Exception e) { return failedForm(dto, "write", model, e); }
    }
    @GetMapping("/article")
    public String article(@RequestParam("noticeId") long id, Model model) throws Exception {
        AdminNoticeDto dto = service.findById(id);
        if (dto == null) return "redirect:/admin/notice/list";
        service.updateHitCount(id);
        model.addAttribute("dto", service.findById(id));
        model.addAttribute("listFile", service.listNoticeFile(id));
        return "admin/layout/noticeArticle";
    }
    @GetMapping("/update")
    public String updateForm(@RequestParam("noticeId") long id, Model model) {
        AdminNoticeDto dto = service.findById(id);
        if (dto == null) return "redirect:/admin/notice/list";
        model.addAttribute("dto", dto); model.addAttribute("mode", "update");
        model.addAttribute("listFile", service.listNoticeFile(id));
        return "admin/layout/noticeWrite";
    }
    @PostMapping("/update")
    public String update(AdminNoticeDto dto, Model model, RedirectAttributes flash) {
        try {
            service.updateNotice(dto, null);
            flash.addFlashAttribute("message", "공지사항이 수정되었습니다.");
            return "redirect:/admin/notice/article?noticeId=" + dto.getNoticeId();
        } catch (Exception e) { return failedForm(dto, "update", model, e); }
    }
    private String failedForm(AdminNoticeDto dto, String mode, Model model, Exception e) {
        log.error("공지 저장 실패 noticeId={}", dto.getNoticeId(), e);
        model.addAttribute("dto", dto); model.addAttribute("mode", mode);
        model.addAttribute("error", "저장하지 못했습니다. 제목·내용과 첨부파일을 확인하고 다시 시도해주세요. 파일은 다시 선택해주세요.");
        model.addAttribute("listFile", "update".equals(mode) ? service.listNoticeFile(dto.getNoticeId()) : List.of());
        return "admin/layout/noticeWrite";
    }
    @PostMapping("/delete")
    public String delete(@RequestParam("noticeId") long id, RedirectAttributes flash) {
        try {
            service.deleteNotice(id, null);
            flash.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("공지 삭제 실패 noticeId={}", id, e);
            flash.addFlashAttribute("error", "공지사항을 삭제하지 못했습니다. 다시 시도해주세요.");
        }
        return "redirect:/admin/notice/list";
    }
    @ResponseBody @PostMapping("/deleteFile")
    public Map<String,Object> deleteFile(@RequestParam("noticeId") long noticeId, @RequestParam("fileId") long fileId) {
        try {
            AdminNoticeDto file = service.findByFileId(fileId);
            if (file == null || file.getNoticeId() != noticeId)
                return Map.of("success", false, "message", "첨부파일을 찾을 수 없습니다.");
            return Map.of("success", service.deleteNoticeFile(fileId, null));
        } catch (Exception e) {
            log.error("공지 첨부 삭제 실패 fileId={}", fileId, e);
            return Map.of("success", false, "message", "첨부파일을 삭제하지 못했습니다.");
        }
    }
    @GetMapping("/download")
    public void download(@RequestParam("fileId") long id, HttpServletResponse response) throws Exception {
        AdminNoticeDto file = service.findByFileId(id);
        if (file == null) { response.sendError(404); return; }
        service.downloadFile(file.getFilePath(), file.getOriginalFileName(), response);
    }
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public String tooLarge(RedirectAttributes flash) {
        flash.addFlashAttribute("error", "첨부파일 용량 제한을 초과해 저장되지 않았습니다. 파일 크기를 줄인 뒤 다시 작성해주세요.");
        return "redirect:/admin/notice/list";
    }
}
