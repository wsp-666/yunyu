package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VenueService {

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

    public IPage<VenueVO> getVenueList(int currentPage, int pageSize, String keyword) {
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
        return result;
    }

    public VenueDetailVO getVenueDetail(int venueId) {
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
    }

    public List<FishingVenue> getMyVenues(Integer ownerId) {
        return venueDao.selectList(new LambdaQueryWrapper<FishingVenue>()
                .eq(FishingVenue::getOwnerId, ownerId));
    }

    public void createVenue(FishingVenue venue) {
        venueDao.insert(venue);
    }

    @Transactional
    public void deleteVenue(Integer id) {
        // 删除关联的放鱼/上鱼视频
        fishVideoDao.delete(new LambdaQueryWrapper<FishVideo>()
                .eq(FishVideo::getVenueId, id));
        // 删除关联的场次
        sessionDao.delete(new LambdaQueryWrapper<Session>()
                .eq(Session::getVenueId, id));
        // 删除关联的订单
        orderDao.delete(new LambdaQueryWrapper<Order>()
                .eq(Order::getVenueId, id));
        // 删除关联的关注（targetType=2 表示钓场）
        followDao.delete(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getTargetType, 2)
                .eq(Follow::getTargetId, id));
        // 删除钓场
        venueDao.deleteById(id);
    }
}
