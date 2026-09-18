package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yunyu.common.CacheKeys;
import com.yunyu.common.PageResult;
import com.yunyu.dao.FishVideoDao;
import com.yunyu.dao.FishingVenueDao;
import com.yunyu.dao.FollowDao;
import com.yunyu.dao.OrderDao;
import com.yunyu.dao.SessionDao;
import com.yunyu.entity.FishVideo;
import com.yunyu.entity.FishingVenue;
import com.yunyu.entity.Follow;
import com.yunyu.entity.Order;
import com.yunyu.entity.Session;
import com.yunyu.vo.VenueDetailVO;
import com.yunyu.vo.VenueVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VenueService {

    private static final Duration LIST_TTL = Duration.ofMinutes(2);
    private static final Duration DETAIL_TTL = Duration.ofMinutes(3);

    @Autowired
    private FishingVenueDao venueDao;

    @Autowired
    private FishVideoDao fishVideoDao;

    @Autowired
    private SessionDao sessionDao;

    @Autowired
    private FollowDao followDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private RedisCacheService redisCacheService;

    public IPage<VenueVO> getVenueList(int currentPage, int pageSize, String keyword) {
        String key = CacheKeys.venueList(currentPage, pageSize, keyword);
        PageResult<VenueVO> cached = redisCacheService.getOrLoad(
                key,
                LIST_TTL,
                new TypeReference<PageResult<VenueVO>>() {},
                () -> {
                    Page<VenueVO> page = new Page<>(currentPage, pageSize);
                    IPage<VenueVO> result = venueDao.selectVenuePage(page, keyword);
                    LocalDateTime now = LocalDateTime.now();
                    for (VenueVO vo : result.getRecords()) {
                        if (vo.getLatestStockTime() != null) {
                            vo.setIsNew(vo.getLatestStockTime().isAfter(now.minusHours(24)));
                        } else {
                            vo.setIsNew(false);
                        }
                    }
                    return PageResult.from(result);
                }
        );

        Page<VenueVO> page = new Page<>(cached.getCurrentPage(), cached.getPageSize(), cached.getTotalCount());
        page.setRecords(cached.getList());
        return page;
    }

    public VenueDetailVO getVenueDetail(int venueId) {
        return redisCacheService.getOrLoad(
                CacheKeys.venueDetail(venueId),
                DETAIL_TTL,
                VenueDetailVO.class,
                () -> loadVenueDetail(venueId)
        );
    }

    private VenueDetailVO loadVenueDetail(int venueId) {
        FishingVenue venue = venueDao.selectById(venueId);
        if (venue == null) {
            throw new IllegalArgumentException("钓场不存在");
        }

        VenueDetailVO detail = new VenueDetailVO();
        detail.setId(venue.getId());
        detail.setName(venue.getName());
        detail.setAddress(venue.getAddress());
        detail.setPhone(venue.getPhone());
        detail.setCoverImage(venue.getCoverImage());
        detail.setSeatMap(venue.getSeatMap());
        detail.setImages(venue.getImages());
        detail.setLongitude(venue.getLongitude());
        detail.setLatitude(venue.getLatitude());
        detail.setRuleDesc(venue.getRuleDesc());
        detail.setTotalSeats(venue.getTotalSeats());
        detail.setFollowerCount(venue.getFollowerCount());
        detail.setStatus(venue.getStatus());

        FishVideo latestStock = fishVideoDao.getLatestStockVideo(venueId);
        detail.setLatestStockVideo(latestStock);

        Page<FishVideo> catchPage = new Page<>(1, 20);
        fishVideoDao.getCatchVideoPage(catchPage, venueId);
        detail.setCatchVideoList(catchPage.getRecords());

        List<Session> sessions = sessionDao.getAvailableSessions(venueId);
        detail.setSessionList(sessions);

        return detail;
    }

    public FishingVenue getVenueById(Integer id) {
        return venueDao.selectById(id);
    }

    public void updateVenue(FishingVenue venue) {
        venueDao.updateById(venue);
        evictVenueCache();
    }

    public List<FishingVenue> getMyVenues(Integer ownerId) {
        return venueDao.selectList(new LambdaQueryWrapper<FishingVenue>()
                .eq(FishingVenue::getOwnerId, ownerId));
    }

    public void createVenue(FishingVenue venue) {
        venueDao.insert(venue);
        evictVenueCache();
    }

    @Transactional
    public void deleteVenue(Integer id) {
        fishVideoDao.delete(new LambdaQueryWrapper<FishVideo>()
                .eq(FishVideo::getVenueId, id));
        sessionDao.delete(new LambdaQueryWrapper<Session>()
                .eq(Session::getVenueId, id));
        orderDao.delete(new LambdaQueryWrapper<Order>()
                .eq(Order::getVenueId, id));
        followDao.delete(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getTargetType, 2)
                .eq(Follow::getTargetId, id));
        venueDao.deleteById(id);
        evictVenueCache();
    }

    private void evictVenueCache() {
        redisCacheService.deleteByPattern(CacheKeys.VENUE_PATTERN);
    }
}
