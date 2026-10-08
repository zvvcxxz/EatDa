package com.sp.app.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sp.app.admin.service.AdminHomeService;

@Controller
@RequestMapping("/admin/*")
public class AdminHomeController {

    @Autowired
    private AdminHomeService service;

    @GetMapping("main")
    public String main(Model model) {
        Map<String, Object> stats = service.getDashboardStats();
        model.addAttribute("stats", stats);
        
        return "admin/main/home";
    }
}
