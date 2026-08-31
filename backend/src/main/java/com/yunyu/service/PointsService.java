package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.dao.PointsLogDao;
import com.yunyu.dao.ProductDao;
import com.yunyu.dao.UserDao;
import com.yunyu.entity.PointsLog;
import com.yunyu.entity.Product;
import com.yunyu.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PointsService {

    @Autowired
    private PointsLogDao pointsLogDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ProductDao productDao;

    @Transactional
    public int addPoints(int userId, int points, String desc, int type, Integer refId) {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPoints(user.getPoints() + points);
        userDao.updateById(user);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setType(type);
        log.setRefId(refId);
        pointsLogDao.insert(log);

        return user.getPoints();
    }

    @Transactional
    public boolean exchangeProduct(int userId, int productId) {
        User user = userDao.selectById(userId);
        Product product = productDao.selectById(productId);

        if (user == null || product == null) {
            throw new IllegalArgumentException("用户或商品不存在");
        }
        if (product.getStock() <= 0) {
            throw new IllegalArgumentException("商品库存不足");
        }
        if (user.getPoints() < product.getPointsPrice()) {
            throw new IllegalArgumentException("积分不足");
        }

        user.setPoints(user.getPoints() - product.getPointsPrice());
        userDao.updateById(user);

        product.setStock(product.getStock() - 1);
        product.setSalesCount(product.getSalesCount() + 1);
        productDao.updateById(product);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(-product.getPointsPrice());
        log.setType(4);
        log.setRefId(productId);
        pointsLogDao.insert(log);

        return true;
    }

    public Page<PointsLog> getPointsLog(int userId, int page, int size) {
        Page<PointsLog> p = new Page<>(page, size);
        return pointsLogDao.selectPage(p, new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreateTime));
    }

    @Transactional
    public Map<String, Object> upgradeMember(int userId, int months) {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 现金价格：1个月30元，6个月108元，12个月188元
        int price;
        switch (months) {
            case 1: price = 30; break;
            case 6: price = 108; break;
            case 12: price = 188; break;
            default: throw new IllegalArgumentException("不支持的会员时长");
        }

        // 设置会员
        user.setMemberLevel(1);
        LocalDateTime now = LocalDateTime.now();
        if (user.getMemberExpire() != null && user.getMemberExpire().isAfter(now)) {
            user.setMemberExpire(user.getMemberExpire().plusMonths(months));
        } else {
            user.setMemberExpire(now.plusMonths(months));
        }
        userDao.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("memberLevel", user.getMemberLevel());
        result.put("memberExpire", user.getMemberExpire());
        result.put("price", price);
        return result;
    }

    public boolean checkDailySignIn(int userId) {
        // 检查今天是否已签到
        Long count = pointsLogDao.selectCount(new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .eq(PointsLog::getType, 3)
                .apply("DATE(create_time) = CURDATE()"));
        return count == 0;
    }
}
