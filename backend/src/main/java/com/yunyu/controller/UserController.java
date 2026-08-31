package com.yunyu.controller;

import com.yunyu.common.Result;
import com.yunyu.dto.LoginDTO;
import com.yunyu.dto.RegisterDTO;
import com.yunyu.entity.User;
import com.yunyu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        Map<String, Object> result = userService.login(dto.getAccount(), dto.getPassword());
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterDTO dto) {
        User user = userService.register(dto.getAccount(), dto.getPassword(), dto.getNickname(), dto.getPhone(), dto.getRole());
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<User> getInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody User user, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        user.setId(userId);
        userService.updateUser(user);
        return Result.success();
    }
}
