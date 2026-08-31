package com.yunyu.interceptor;

import com.yunyu.common.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或token已过期\"}");
            return false;
        }
        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token无效或已过期\"}");
            return false;
        }
        Integer userId = jwtUtil.getUserId(token);
        request.setAttribute("userId", userId);
        Integer adminRole = jwtUtil.getAdminRole(token);
        if (adminRole != null) {
            request.setAttribute("adminRole", adminRole);
        }
        return true;
    }
}
