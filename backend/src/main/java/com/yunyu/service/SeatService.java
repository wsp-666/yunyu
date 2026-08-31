package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunyu.dao.OrderDao;
import com.yunyu.dao.SessionDao;
import com.yunyu.entity.Order;
import com.yunyu.entity.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class SeatService {

    @Autowired
    private SessionDao sessionDao;

    @Autowired
    private OrderDao orderDao;

    private final Random random = new Random();

    @Transactional
    public void assignSeats(int sessionId) {
        Session session = sessionDao.selectById(sessionId);
        if (session == null) return;

        List<Order> orders = orderDao.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getSessionId, sessionId)
                .eq(Order::getPayStatus, 1)
                .isNull(Order::getSeatNo));

        int totalSeats = session.getTotalSeats();
        boolean[] usedSeats = new boolean[totalSeats + 1];

        // 标记已分配的座位
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

    // 定时任务：每分钟检查需要抽位的场次
    @Scheduled(fixedRate = 60000)
    public void autoAssignSeats() {
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
