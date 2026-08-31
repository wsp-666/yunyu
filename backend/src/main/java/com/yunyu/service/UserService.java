package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunyu.common.JwtUtil;
import com.yunyu.dao.AdminDao;
import com.yunyu.dao.UserDao;
import com.yunyu.entity.Admin;
import com.yunyu.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> login(String account, String password) {
        // 先查用户表
        User user = userDao.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, account));
        if (user != null) {
            if ("0".equals(user.getLockState())) {
                throw new IllegalArgumentException("账号已被锁定");
            }
            String md5Pwd = DigestUtils.md5DigestAsHex(password.getBytes());
            if (!user.getPassword().equals(md5Pwd)) {
                throw new IllegalArgumentException("密码错误");
            }
            user.setLastLoginTime(LocalDateTime.now());
            userDao.updateById(user);

            String token = jwtUtil.generateToken(user.getId(), user.getAccount());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userType", "user");
            result.put("userId", user.getId());
            result.put("nickname", user.getNickname());
            result.put("avatar", user.getAvatar());
            result.put("points", user.getPoints());
            result.put("memberLevel", user.getMemberLevel());
            result.put("memberExpire", user.getMemberExpire());
            result.put("role", user.getRole());
            return result;
        }

        // 再查管理员表
        Admin admin = adminDao.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getAccount, account));
        if (admin != null) {
            String md5Pwd = DigestUtils.md5DigestAsHex(password.getBytes());
            if (!admin.getPassword().equals(md5Pwd)) {
                throw new IllegalArgumentException("密码错误");
            }
            String token = jwtUtil.generateAdminToken(admin.getId(), admin.getAccount(), admin.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userType", "admin");
            result.put("userId", admin.getId());
            result.put("nickname", admin.getName());
            result.put("adminRole", admin.getRole());
            return result;
        }

        throw new IllegalArgumentException("账号不存在");
    }

    public User register(String account, String password, String nickname, String phone, Integer role) {
        User exist = userDao.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, account));
        if (exist != null) {
            throw new IllegalArgumentException("账号已存在");
        }
        User user = new User();
        user.setAccount(account);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setPoints(0);
        user.setMemberLevel(0);
        user.setRole(role != null ? role : 0);
        user.setLockState("1");
        userDao.insert(user);
        return user;
    }

    public User getUserById(Integer id) {
        return userDao.selectById(id);
    }

    public void updateUser(User user) {
        userDao.updateById(user);
    }
}
