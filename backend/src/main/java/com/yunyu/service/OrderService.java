package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunyu.common.CacheKeys;
import com.yunyu.dao.OrderDao;
import com.yunyu.dao.SessionDao;
import com.yunyu.dao.UserDao;
import com.yunyu.entity.Order;
import com.yunyu.entity.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SessionDao sessionDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DistributedLockService distributedLockService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RateLimitService rateLimitService;

    @Transactional
    public int createOrder(int userId, int sessionId) {
        if (!rateLimitService.tryAcquire("order:create", String.valueOf(userId), 10, Duration.ofMinutes(1))) {
            throw new IllegalStateException("下单过于频繁，请稍后再试");
        }

        return distributedLockService.executeWithLock(
                CacheKeys.lockSessionOrder(sessionId),
                Duration.ofSeconds(8),
                3000,
                () -> doCreateOrder(userId, sessionId)
        );
    }

    private int doCreateOrder(int userId, int sessionId) {
        Session session = sessionDao.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("场次不存在");
        }

        // 乐观扣减余票，避免超卖
        int updated = sessionDao.update(null, new LambdaUpdateWrapper<Session>()
                .eq(Session::getId, sessionId)
                .gt(Session::getRemainSeats, 0)
                .setSql("remain_seats = remain_seats - 1"));
        if (updated == 0) {
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

        stringRedisTemplate.delete(CacheKeys.venueDetail(session.getVenueId()));
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

        int sessionId = order.getSessionId();
        return distributedLockService.executeWithLock(
                CacheKeys.lockSessionSeat(sessionId),
                Duration.ofSeconds(10),
                4000,
                () -> doDrawSeat(orderId, sessionId)
        );
    }

    private int doDrawSeat(int orderId, int sessionId) {
        Order order = orderDao.selectById(orderId);
        if (order.getSeatNo() != null) {
            return order.getSeatNo();
        }
        Session session = sessionDao.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("场次不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (session.getDrawTime() != null && now.isBefore(session.getDrawTime())) {
            throw new IllegalArgumentException("尚未到抽号时间");
        }
        if (session.getEndTime() != null && now.isAfter(session.getEndTime())) {
            throw new IllegalArgumentException("不在抽号时间内");
        }

        List<Integer> used = orderDao.selectList(new LambdaQueryWrapper<Order>()
                        .eq(Order::getSessionId, sessionId)
                        .isNotNull(Order::getSeatNo))
                .stream()
                .map(Order::getSeatNo)
                .collect(Collectors.toList());

        int total = session.getTotalSeats() == null ? 0 : session.getTotalSeats();
        if (total <= 0) {
            throw new IllegalArgumentException("场次钓位配置异常");
        }

        java.util.List<Integer> free = new java.util.ArrayList<>();
        for (int i = 1; i <= total; i++) {
            if (!used.contains(i)) {
                free.add(i);
            }
        }
        if (free.isEmpty()) {
            throw new IllegalArgumentException("暂无可用钓位");
        }

        int seatNo = free.get((int) (Math.random() * free.size()));
        order.setSeatNo(seatNo);
        orderDao.updateById(order);

        stringRedisTemplate.opsForSet().add(CacheKeys.sessionSeats(sessionId), String.valueOf(seatNo));
        return seatNo;
    }
}
