package com.yunyu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.common.PageResult;
import com.yunyu.common.Result;
import com.yunyu.entity.PointsLog;
import com.yunyu.entity.User;
import com.yunyu.service.PointsService;
import com.yunyu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private UserService userService;

    @GetMapping("/detail")
    public Result<Map<String, Object>> getPointsDetail(HttpServletRequest request,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Integer userId = (Integer) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        Page<PointsLog> logPage = pointsService.getPointsLog(userId, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("totalPoints", user.getPoints());
        result.put("pointsLogList", PageResult.from(logPage));
        return Result.success(result);
    }

    @PostMapping("/signin")
    public Result<Map<String, Object>> dailySignIn(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (!pointsService.checkDailySignIn(userId)) {
            return Result.error("今天已签到");
        }
        int newPoints = pointsService.addPoints(userId, 5, "每日签到", 3, null);
        Map<String, Object> result = new HashMap<>();
        result.put("points", 5);
        result.put("totalPoints", newPoints);
        return Result.success(result);
    }

    @PostMapping("/exchange")
    public Result<Void> exchangeProduct(@RequestParam int productId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        pointsService.exchangeProduct(userId, productId);
        return Result.success();
    }

    @PostMapping("/upgrade-member")
    public Result<Map<String, Object>> upgradeMember(@RequestParam int months, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Map<String, Object> result = pointsService.upgradeMember(userId, months);
        return Result.success(result);
    }

    @GetMapping("/member-info")
    public Result<Map<String, Object>> getMemberInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("memberLevel", user.getMemberLevel());
        result.put("memberExpire", user.getMemberExpire());
        result.put("points", user.getPoints());
        return Result.success(result);
    }
}
