package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunyu.dao.OrderDao;
import com.yunyu.dao.SessionDao;
import com.yunyu.dao.UserDao;
import com.yunyu.entity.Order;
import com.yunyu.entity.Session;
import com.yunyu.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SessionDao sessionDao;

    @Autowired
    private UserDao userDao;

    @Transactional
    public int createOrder(int userId, int sessionId) {
        Session session = sessionDao.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("场次不存在");
        }
        if (session.getRemainSeats() <= 0) {
            throw new IllegalArgumentException("该场次已满员");
        }

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        order.setUserId(userId);
        order.setSessionId(sessionId);
        order.setVenueId(session.getVenueId());
        order.setAmount(session.getTicketPrice());
        order.setPayStatus(0);
        orderDao.insert(order);

        session.setRemainSeats(session.getRemainSeats() - 1);
        sessionDao.updateById(session);

        return order.getId();
    }

    @Transactional
    public boolean payOrder(int orderId) {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getPayStatus() == 1) {
            throw new IllegalArgumentException("订单已支付");
        }
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderDao.updateById(order);
        return true;
    }

    public Order getOrderById(int orderId) {
        return orderDao.selectById(orderId);
    }

    public Order getOrderByUserAndSession(int userId, int sessionId) {
        return orderDao.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getSessionId, sessionId)
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 1"));
    }

    @Transactional
    public int drawSeat(int orderId, int userId) {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getUserId() != userId) {
            throw new IllegalArgumentException("无权操作此订单");
        }
        if (order.getPayStatus() != 1) {
            throw new IllegalArgumentException("订单未支付");
        }
        if (order.getSeatNo() != null) {
            throw new IllegalArgumentException("已抽过号");
        }
        Session session = sessionDao.selectById(order.getSessionId());
        if (session == null) {
            throw new IllegalArgumentException("场次不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getDrawTime())) {
            throw new IllegalArgumentException("尚未到抽号时间");
        }
        if (now.isAfter(session.getEndTime())) {
            throw new IllegalArgumentException("不在抽号时间内");
        }
        // 随机分配钓位
        int seatNo = (int)(Math.random() * session.getTotalSeats()) + 1;
        order.setSeatNo(seatNo);
        orderDao.updateById(order);
        return seatNo;
    }
}
