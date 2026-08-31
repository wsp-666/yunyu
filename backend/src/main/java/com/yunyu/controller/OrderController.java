package com.yunyu.controller;

import com.yunyu.common.Result;
import com.yunyu.dto.OrderDTO;
import com.yunyu.entity.Order;
import com.yunyu.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestBody OrderDTO dto, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        int orderId = orderService.createOrder(userId, dto.getSessionId());
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<Void> payOrder(@RequestParam int orderId) {
        orderService.payOrder(orderId);
        return Result.success();
    }

    @GetMapping("/seat")
    public Result<Order> getOrderSeat(@RequestParam int orderId) {
        Order order = orderService.getOrderById(orderId);
        return Result.success(order);
    }

    @PostMapping("/draw")
    public Result<Map<String, Object>> drawSeat(@RequestParam int orderId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        int seatNo = orderService.drawSeat(orderId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("seatNo", seatNo);
        return Result.success(result);
    }

    @GetMapping("/my-session")
    public Result<Order> getMySessionOrder(@RequestParam int sessionId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Order order = orderService.getOrderByUserAndSession(userId, sessionId);
        return Result.success(order);
    }
}
