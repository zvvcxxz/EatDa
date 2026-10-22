package com.sp.app.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sp.app.admin.domain.dto.AdminIngredientDto;
import com.sp.app.admin.service.AdminIngredientService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/ingredient")
public class AdminIngredientController {

    @Autowired
    private AdminIngredientService service;

    @GetMapping({"", "/list"})
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int current_page,
            @RequestParam(value = "schType", defaultValue = "ingredientName") String schType,
            @RequestParam(value = "kwd", defaultValue = "") String kwd,
            HttpServletRequest req,
            Model model) {
        
        int size = 15;
        int offset = (current_page - 1) * size;
        
        Map<String, Object> map = new HashMap<>();
        map.put("offset", offset);
        map.put("size", size);
        map.put("schType", schType);
        map.put("kwd", kwd);
        
        int dataCount = service.dataCount(map);
        int total_page = (int) Math.ceil((double) dataCount / size);
        
        if (current_page > total_page && total_page > 0) {
            current_page = total_page;
            map.put("offset", (current_page - 1) * size);
        }
        
        List<AdminIngredientDto> list = service.listIngredient(map);
        
        model.addAttribute("list", list);
        model.addAttribute("page", current_page);
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("size", size);
        model.addAttribute("total_page", total_page);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        
        return "admin/layout/ingredientList";
    }

    @PostMapping("/write")
    public String writeSubmit(AdminIngredientDto dto, RedirectAttributes reAttr) {
        try {
            AdminIngredientDto exist = service.findByName(dto.getIngredientName());
            if(exist != null) {
                return "redirect:/admin/ingredient/list";
            }
            service.insertIngredient(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/ingredient/list";
    }

    @PostMapping("/update")
    public String updateSubmit(
            AdminIngredientDto dto,
            @RequestParam(value = "page", defaultValue = "1") String page,
            RedirectAttributes reAttr) {
        try {
            service.updateIngredient(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        reAttr.addAttribute("page", page);
        return "redirect:/admin/ingredient/list";
    }
}