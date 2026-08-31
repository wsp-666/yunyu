package com.yunyu.service;

import com.yunyu.dao.OrderDao;
import com.yunyu.vo.RevenueStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StatService {

    @Autowired
    private OrderDao orderDao;

    public RevenueStatsVO getRevenueStats(int venueId, String startDate, String endDate, String groupBy) {
        RevenueStatsVO vo = new RevenueStatsVO();

        // 营收趋势
        List<Map<String, Object>> revenueMaps = orderDao.selectRevenueByDay(venueId, startDate, endDate);
        List<RevenueStatsVO.RevenueData> revenueDataList = new ArrayList<>();
        for (Map<String, Object> map : revenueMaps) {
            RevenueStatsVO.RevenueData data = new RevenueStatsVO.RevenueData();
            data.setDate((String) map.get("date"));
            data.setAmount((BigDecimal) map.get("amount"));
            data.setOrderCount(((Number) map.get("orderCount")).intValue());
            revenueDataList.add(data);
        }
        vo.setRevenueData(revenueDataList);

        // 场次统计
        List<Map<String, Object>> sessionMaps = orderDao.selectSessionStats(venueId);
        List<RevenueStatsVO.SessionStats> sessionStatsList = new ArrayList<>();
        for (Map<String, Object> map : sessionMaps) {
            RevenueStatsVO.SessionStats stats = new RevenueStatsVO.SessionStats();
            stats.setSessionName((String) map.get("sessionName"));
            stats.setRevenue((BigDecimal) map.get("revenue"));
            stats.setTotalSeats(((Number) map.get("totalSeats")).intValue());
            stats.setBookedSeats(((Number) map.get("bookedSeats")).intValue());
            int total = stats.getTotalSeats();
            int booked = stats.getBookedSeats();
            stats.setOccupancyRate(total > 0 ? (double) booked / total * 100 : 0);
            sessionStatsList.add(stats);
        }
        vo.setSessionStats(sessionStatsList);

        return vo;
    }
}
