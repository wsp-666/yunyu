package com.yunyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunyu.common.Result;
import com.yunyu.dao.FollowDao;
import com.yunyu.dao.FishingVenueDao;
import com.yunyu.entity.Follow;
import com.yunyu.entity.FishingVenue;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private FollowDao followDao;

    @Autowired
    private FishingVenueDao venueDao;

    @PostMapping("/toggle")
    public Result<Boolean> toggleFollow(@RequestParam Integer targetType,
                                        @RequestParam Integer targetId,
                                        HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Follow exist = followDao.checkFollow(userId, targetType, targetId);
        if (exist != null) {
            followDao.deleteById(exist.getId());
            if (targetType == 2) {
                FishingVenue venue = venueDao.selectById(targetId);
                if (venue != null) {
                    venue.setFollowerCount(Math.max(0, venue.getFollowerCount() - 1));
                    venueDao.updateById(venue);
                }
            }
            return Result.success(false);
        } else {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setTargetType(targetType);
            follow.setTargetId(targetId);
            followDao.insert(follow);
            if (targetType == 2) {
                FishingVenue venue = venueDao.selectById(targetId);
                if (venue != null) {
                    venue.setFollowerCount(venue.getFollowerCount() + 1);
                    venueDao.updateById(venue);
                }
            }
            return Result.success(true);
        }
    }

    @GetMapping("/check")
    public Result<Boolean> checkFollow(@RequestParam Integer targetType,
                                       @RequestParam Integer targetId,
                                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Follow exist = followDao.checkFollow(userId, targetType, targetId);
        return Result.success(exist != null);
    }
}
