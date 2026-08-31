package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.dao.FishingSpotDao;
import com.yunyu.entity.FishingSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpotService {

    @Autowired
    private FishingSpotDao fishingSpotDao;

    public IPage<FishingSpot> listApprovedSpots(int currentPage, int pageSize, String keyword) {
        Page<FishingSpot> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<FishingSpot> wrapper = new LambdaQueryWrapper<FishingSpot>()
                .eq(FishingSpot::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(FishingSpot::getName, keyword)
                    .or().like(FishingSpot::getFishSpecies, keyword)
                    .or().like(FishingSpot::getEnvDesc, keyword));
        }
        wrapper.orderByDesc(FishingSpot::getCreateTime);
        return fishingSpotDao.selectPage(page, wrapper);
    }

    public FishingSpot getSpotDetail(int id) {
        FishingSpot spot = fishingSpotDao.selectById(id);
        if (spot == null) {
            throw new IllegalArgumentException("钓点不存在");
        }
        spot.setViewCount(spot.getViewCount() + 1);
        fishingSpotDao.updateById(spot);
        return spot;
    }

    public void createSpot(FishingSpot spot, Integer userId) {
        spot.setSubmitUserId(userId);
        spot.setStatus(0);
        spot.setViewCount(0);
        fishingSpotDao.insert(spot);
    }
}
