package com.sp.app.admin.controller;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sp.app.admin.domain.dto.AdminRecipeDto;
import com.sp.app.admin.service.AdminRecipeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/recipe")
@Slf4j
public class AdminRecipeController {
	
	private final AdminRecipeService service;
	
	@GetMapping(value = {"", "/", "list"})
	public String list(
			@RequestParam(name = "page", defaultValue = "1") int current_page,
			@RequestParam(name = "schType", defaultValue = "title") String schType,
			@RequestParam(name = "kwd", defaultValue = "") String kwd,
            @RequestParam(name = "sourceType", defaultValue = "") String sourceType,
            Model model) throws Exception {
		
		if (kwd != null && !kwd.isEmpty()) {
			kwd = URLDecoder.decode(kwd, "UTF-8");
		}
		
		int size = 10;
		int total_page = 0;
		int dataCount = 0;
		
		Map<String, Object> map = new HashMap<>();
		map.put("schType", schType);
		map.put("kwd", kwd);
		map.put("sourceType", sourceType);
		
		dataCount = service.dataCount(map);
		
		if (dataCount != 0) {
			total_page = (dataCount / size) + (dataCount % size > 0 ? 1 : 0);
		}
		
		if (current_page > total_page) {
			current_page = total_page;
		}
		
		int offset = (current_page - 1) * size;
		if (offset < 0) offset = 0;
		
		map.put("offset", offset);
		map.put("size", size);
		
		List<AdminRecipeDto> list = service.listRecipe(map);
		
		model.addAttribute("list", list);
		model.addAttribute("page", current_page);
		model.addAttribute("dataCount", dataCount);
		model.addAttribute("size", size);
		model.addAttribute("total_page", total_page);
		
		model.addAttribute("schType", schType);
		model.addAttribute("kwd", kwd);
		model.addAttribute("sourceType", sourceType);
		
		return "admin/layout/recipeList";
	}
	
	@GetMapping("article")
    public String article(
            @RequestParam(name = "recipeId") Long recipeId,
            @RequestParam(name = "page") String page,
            @RequestParam(name = "schType", defaultValue = "title") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            @RequestParam(name = "sourceType", defaultValue = "") String sourceType,
            Model model) throws Exception {

        if (kwd != null && !kwd.isEmpty()) {
            kwd = URLDecoder.decode(kwd, "UTF-8");
        }

        AdminRecipeDto dto = service.findById(recipeId);

        if (dto == null) {
            return "redirect:/admin/recipe/list?page=" + page;
        }

        model.addAttribute("dto", dto);
        model.addAttribute("page", page);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        model.addAttribute("sourceType", sourceType);

        return "admin/layout/recipeArticle";
    }
	
	@PostMapping("updateStatus")
	public String updateStatus(
			@RequestParam(name = "recipeId") Long recipeId,
			@RequestParam(name = "status") int status,
			@RequestParam(name = "page") String page,
			@RequestParam(name = "schType", defaultValue = "title") String schType,
			@RequestParam(name = "kwd", defaultValue = "") String kwd,
			@RequestParam(name = "sourceType", defaultValue = "") String sourceType,
			RedirectAttributes reAttr) throws Exception {
		
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("recipeId", recipeId);
			map.put("status", status);
			
			service.updateRecipeStatus(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		reAttr.addAttribute("page", page);
		reAttr.addAttribute("schType", schType);
		reAttr.addAttribute("kwd", kwd);
		reAttr.addAttribute("sourceType", sourceType);
		
		return "redirect:/admin/recipe/list";
	}
}