package com.yunyu.controller;

import com.yunyu.common.Result;
import com.yunyu.entity.Session;
import com.yunyu.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping("/{id}")
    public Result<Session> getSession(@PathVariable int id) {
        return Result.success(sessionService.getSessionById(id));
    }

    @GetMapping("/list/{venueId}")
    public Result<List<Session>> getSessions(@PathVariable int venueId) {
        return Result.success(sessionService.getSessionsByVenue(venueId));
    }

    @PostMapping("/create")
    public Result<Session> createSession(@RequestBody Session session) {
        return Result.success(sessionService.createSession(session));
    }

    @PutMapping("/update")
    public Result<Void> updateSession(@RequestBody Session session) {
        sessionService.updateSession(session);
        return Result.success();
    }
}
