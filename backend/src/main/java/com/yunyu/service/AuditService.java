package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.dao.FishingSpotDao;
import com.yunyu.dao.PostDao;
import com.yunyu.dto.AuditDTO;
import com.yunyu.entity.FishingSpot;
import com.yunyu.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    @Autowired
    private FishingSpotDao fishingSpotDao;

    @Autowired
    private PostDao postDao;

    @Autowired
    private PointsService pointsService;

    public Page<?> getPendingList(String type, int page, int size) {
        if ("spot".equals(type)) {
            Page<FishingSpot> p = new Page<>(page, size);
            return fishingSpotDao.selectPage(p, new LambdaQueryWrapper<FishingSpot>()
                    .eq(FishingSpot::getStatus, 0)
                    .orderByDesc(FishingSpot::getCreateTime));
        } else if ("post".equals(type)) {
            Page<Post> p = new Page<>(page, size);
            return postDao.selectPage(p, new LambdaQueryWrapper<Post>()
                    .eq(Post::getStatus, 0)
                    .orderByDesc(Post::getCreateTime));
        }
        throw new IllegalArgumentException("审核类型错误");
    }

    @Transactional
    public boolean audit(AuditDTO dto) {
        if ("spot".equals(dto.getTargetType())) {
            FishingSpot spot = fishingSpotDao.selectById(dto.getTargetId());
            if (spot == null) {
                throw new IllegalArgumentException("钓点不存在");
            }
            if ("approve".equals(dto.getAction())) {
                spot.setStatus(1);
                // 审核通过奖励积分
                pointsService.addPoints(spot.getSubmitUserId(), 10, "提交钓点审核通过", 5, spot.getId());
            } else if ("reject".equals(dto.getAction())) {
                spot.setStatus(2);
                spot.setRejectReason(dto.getReason());
            }
            fishingSpotDao.updateById(spot);
            return true;
        } else if ("post".equals(dto.getTargetType())) {
            Post post = postDao.selectById(dto.getTargetId());
            if (post == null) {
                throw new IllegalArgumentException("帖子不存在");
            }
            if ("approve".equals(dto.getAction())) {
                post.setStatus(1);
            } else if ("reject".equals(dto.getAction())) {
                post.setStatus(2);
            }
            postDao.updateById(post);
            return true;
        }
        throw new IllegalArgumentException("审核类型错误");
    }
}
