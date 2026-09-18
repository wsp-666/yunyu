package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yunyu.common.CacheKeys;
import com.yunyu.common.PageResult;
import com.yunyu.dao.FishingSpotDao;
import com.yunyu.entity.FishingSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SpotService {

    @Autowired
    private FishingSpotDao fishingSpotDao;

    @Autowired
    private RedisCacheService redisCacheService;

    public IPage<FishingSpot> listApprovedSpots(int currentPage, int pageSize, String keyword) {
        String key = CacheKeys.spotList(currentPage, pageSize, keyword);
        PageResult<FishingSpot> cached = redisCacheService.getOrLoad(
                key,
                Duration.ofMinutes(2),
                new TypeReference<PageResult<FishingSpot>>() {},
                () -> {
                    Page<FishingSpot> page = new Page<>(currentPage, pageSize);
                    LambdaQueryWrapper<FishingSpot> wrapper = new LambdaQueryWrapper<FishingSpot>()
                            .eq(FishingSpot::getStatus, 1);
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        wrapper.and(w -> w.like(FishingSpot::getName, keyword)
                                .or().like(FishingSpot::getFishSpecies, keyword)
                                .or().like(FishingSpot::getEnvDesc, keyword));
                    }
                    wrapper.orderByDesc(FishingSpot::getCreateTime);
                    return PageResult.from(fishingSpotDao.selectPage(page, wrapper));
                }
        );
        Page<FishingSpot> page = new Page<>(cached.getCurrentPage(), cached.getPageSize(), cached.getTotalCount());
        page.setRecords(cached.getList());
        return page;
    }

    public FishingSpot getSpotDetail(int id) {
        FishingSpot spot = redisCacheService.getOrLoad(
                CacheKeys.spotDetail(id),
                Duration.ofMinutes(5),
                FishingSpot.class,
                () -> {
                    FishingSpot s = fishingSpotDao.selectById(id);
                    if (s == null) {
                        throw new IllegalArgumentException("钓点不存在");
                    }
                    return s;
                }
        );
        // 浏览量异步感：直接库内 +1，并刷新详情缓存
        spot.setViewCount((spot.getViewCount() == null ? 0 : spot.getViewCount()) + 1);
        fishingSpotDao.updateById(spot);
        redisCacheService.put(CacheKeys.spotDetail(id), spot, Duration.ofMinutes(5));
        return spot;
    }

    public void createSpot(FishingSpot spot, Integer userId) {
        spot.setSubmitUserId(userId);
        spot.setStatus(0);
        spot.setViewCount(0);
        fishingSpotDao.insert(spot);
        redisCacheService.deleteByPattern(CacheKeys.SPOT_PATTERN);
    }
}
