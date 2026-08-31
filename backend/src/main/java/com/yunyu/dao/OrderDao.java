package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyu.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDao extends BaseMapper<Order> {

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, " +
            "       SUM(amount) AS amount, " +
            "       COUNT(*) AS orderCount " +
            "FROM t_order " +
            "WHERE venue_id = #{venueId} AND pay_status = 1 " +
            "AND create_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') " +
            "ORDER BY date")
    List<Map<String, Object>> selectRevenueByDay(@Param("venueId") Integer venueId,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    @Select("SELECT s.name AS sessionName, " +
            "       COUNT(o.id) * s.ticket_price AS revenue, " +
            "       s.total_seats AS totalSeats, " +
            "       COUNT(o.id) AS bookedSeats " +
            "FROM t_session s " +
            "LEFT JOIN t_order o ON s.id = o.session_id AND o.pay_status = 1 " +
            "WHERE s.venue_id = #{venueId} " +
            "GROUP BY s.id, s.name, s.total_seats, s.ticket_price")
    List<Map<String, Object>> selectSessionStats(@Param("venueId") Integer venueId);
}
