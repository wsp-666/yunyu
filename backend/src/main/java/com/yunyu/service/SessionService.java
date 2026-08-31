package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunyu.dao.SessionDao;
import com.yunyu.entity.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {

    @Autowired
    private SessionDao sessionDao;

    public Session getSessionById(int id) {
        return sessionDao.selectById(id);
    }

    public List<Session> getSessionsByVenue(int venueId) {
        return sessionDao.selectList(new LambdaQueryWrapper<Session>()
                .eq(Session::getVenueId, venueId)
                .orderByDesc(Session::getStartTime));
    }

    public Session createSession(Session session) {
        session.setRemainSeats(session.getTotalSeats());
        session.setStatus(1);
        sessionDao.insert(session);
        return session;
    }

    public void updateSession(Session session) {
        sessionDao.updateById(session);
    }
}
