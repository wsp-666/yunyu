package com.yunyu.controller;

import com.yunyu.common.Result;
import com.yunyu.service.StatService;
import com.yunyu.vo.RevenueStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stat")
public class StatController {

    @Autowired
    private StatService statService;

    @GetMapping("/revenue")
    public Result<RevenueStatsVO> getRevenueStats(
            @RequestParam int venueId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "day") String groupBy) {
        RevenueStatsVO stats = statService.getRevenueStats(venueId, startDate, endDate, groupBy);
        return Result.success(stats);
    }
}
