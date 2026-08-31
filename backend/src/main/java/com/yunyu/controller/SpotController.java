package com.yunyu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yunyu.common.PageResult;
import com.yunyu.common.Result;
import com.yunyu.entity.FishingSpot;
import com.yunyu.service.SpotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spot")
public class SpotController {

    @Autowired
    private SpotService spotService;

    @GetMapping("/list")
    public Result<PageResult<FishingSpot>> listApprovedSpots(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<FishingSpot> page = spotService.listApprovedSpots(currentPage, pageSize, keyword);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/detail/{id}")
    public Result<FishingSpot> getSpotDetail(@PathVariable int id) {
        return Result.success(spotService.getSpotDetail(id));
    }

    @PostMapping("/create")
    public Result<FishingSpot> createSpot(@RequestBody FishingSpot spot, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        spotService.createSpot(spot, userId);
        return Result.success(spot);
    }
}
