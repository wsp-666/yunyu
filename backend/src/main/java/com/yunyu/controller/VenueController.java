package com.yunyu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yunyu.common.PageResult;
import com.yunyu.common.Result;
import com.yunyu.entity.FishingVenue;
import com.yunyu.service.VenueService;
import com.yunyu.vo.VenueDetailVO;
import com.yunyu.vo.VenueVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @GetMapping("/list")
    public Result<PageResult<VenueVO>> getVenueList(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<VenueVO> page = venueService.getVenueList(currentPage, pageSize, keyword);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/detail/{venueId}")
    public Result<VenueDetailVO> getVenueDetail(@PathVariable int venueId) {
        VenueDetailVO detail = venueService.getVenueDetail(venueId);
        return Result.success(detail);
    }

    @GetMapping("/my")
    public Result<List<FishingVenue>> getMyVenues(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(venueService.getMyVenues(userId));
    }

    @PostMapping("/create")
    public Result<FishingVenue> createVenue(@RequestBody FishingVenue venue, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        venue.setOwnerId(userId);
        venue.setStatus(1);
        venue.setFollowerCount(0);
        venueService.createVenue(venue);
        return Result.success(venue);
    }

    @PutMapping("/update")
    public Result<Void> updateVenue(@RequestBody FishingVenue venue, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        FishingVenue exist = venueService.getVenueById(venue.getId());
        if (exist == null || !exist.getOwnerId().equals(userId)) {
            return Result.error("无权操作");
        }
        venueService.updateVenue(venue);
        return Result.success();
    }

    @DeleteMapping("/delete/{venueId}")
    public Result<Void> deleteVenue(@PathVariable int venueId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        FishingVenue exist = venueService.getVenueById(venueId);
        if (exist == null || !exist.getOwnerId().equals(userId)) {
            return Result.error("无权操作");
        }
        venueService.deleteVenue(venueId);
        return Result.success();
    }
}
