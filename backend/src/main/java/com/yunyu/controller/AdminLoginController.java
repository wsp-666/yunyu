package com.yunyu.controller;

import com.yunyu.common.JwtUtil;
import com.yunyu.common.Result;
import com.yunyu.entity.Admin;
import com.yunyu.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminLoginController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String loginAct, @RequestParam String loginPwd) {
        Admin admin = adminService.login(loginAct, loginPwd);
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getAccount(), admin.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminInfo", admin);
        return Result.success(result);
    }
}
