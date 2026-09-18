package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunyu.common.CacheKeys;
import com.yunyu.dao.OrderDao;
import com.yunyu.dao.SessionDao;
import com.yunyu.entity.Order;
import com.yunyu.entity.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Random;

@Service
public class SeatService {

    @Autowired
    private SessionDao sessionDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private DistributedLockService distributedLockService;

    private final Random random = new Random();

    @Transactional
    public void assignSeats(int sessionId) {
        distributedLockService.executeWithLock(
                CacheKeys.lockSessionSeat(sessionId),
                Duration.ofSeconds(30),
                2000,
                () -> {
                    doAssignSeats(sessionId);
                    return null;
                }
        );
    }

    private void doAssignSeats(int sessionId) {
        Session session = sessionDao.selectById(sessionId);
        if (session == null) return;

        List<Order> orders = orderDao.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getSessionId, sessionId)
                .eq(Order::getPayStatus, 1)
                .isNull(Order::getSeatNo));

        int totalSeats = session.getTotalSeats();
        boolean[] usedSeats = new boolean[totalSeats + 1];

        List<Order> assignedOrders = orderDao.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getSessionId, sessionId)
                .isNotNull(Order::getSeatNo));
        for (Order o : assignedOrders) {
            if (o.getSeatNo() != null && o.getSeatNo() > 0 && o.getSeatNo() <= totalSeats) {
                usedSeats[o.getSeatNo()] = true;
            }
        }

        for (Order order : orders) {
            int seatNo;
            int attempts = 0;
            do {
                seatNo = random.nextInt(totalSeats) + 1;
                attempts++;
            } while (usedSeats[seatNo] && attempts < totalSeats * 2);

            if (!usedSeats[seatNo]) {
                usedSeats[seatNo] = true;
                order.setSeatNo(seatNo);
                orderDao.updateById(order);
            }
        }
    }

    /** 多实例部署时仅一个节点执行（Redis 分布式锁） */
    @Scheduled(fixedRate = 60000)
    public void autoAssignSeats() {
        if (!distributedLockService.tryLock(CacheKeys.lockJob("autoAssignSeats"), Duration.ofSeconds(50))) {
            return;
        }
        List<Session> sessions = sessionDao.selectList(new LambdaQueryWrapper<Session>()
                .eq(Session::getStatus, 2)
                .isNotNull(Session::getDrawTime)
                .le(Session::getDrawTime, java.time.LocalDateTime.now()));
        for (Session session : sessions) {
            assignSeats(session.getId());
            session.setStatus(3);
            sessionDao.updateById(session);
        }
    }
}
