package com.sp.app.admin.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sp.app.admin.service.AdminHomeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeManageController {
    
    private final AdminHomeService service;
	 
    @GetMapping("/admin")
    public String handleHome(Model model) {
        
        Map<String, Object> stats = service.getDashboardStats();
        
        model.addAttribute("stats", stats);
        
        return "admin/main/home";
    }
}