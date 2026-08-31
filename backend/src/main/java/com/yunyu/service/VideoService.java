package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunyu.dao.FishVideoDao;
import com.yunyu.dao.FollowDao;
import com.yunyu.dao.MessageDao;
import com.yunyu.dto.VideoDTO;
import com.yunyu.entity.FishVideo;
import com.yunyu.entity.Follow;
import com.yunyu.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VideoService {

    @Autowired
    private FishVideoDao fishVideoDao;

    @Autowired
    private FollowDao followDao;

    @Autowired
    private MessageDao messageDao;

    @Transactional
    public int uploadVideo(String videoUrl, String coverUrl, VideoDTO dto, int userId) {
        FishVideo video = new FishVideo();
        video.setVenueId(dto.getVenueId());
        video.setType(dto.getType());
        video.setVideoUrl(videoUrl);
        video.setCoverUrl(coverUrl);
        video.setUploadUserId(userId);

        if (dto.getType() == 1) {
            video.setFishCount(dto.getFishCount());
            video.setFishSpecies(dto.getFishSpecies());
            video.setFishSizeDesc(dto.getFishSizeDesc());
            video.setFishingTime(dto.getFishingTime());
            video.setTicketPrice(dto.getTicketPrice());
        }

        fishVideoDao.insert(video);

        // 放鱼视频推送通知给关注者
        if (dto.getType() == 1) {
            pushStockNotification(dto.getVenueId());
        }

        return video.getId();
    }

    private void pushStockNotification(int venueId) {
        List<Follow> follows = followDao.selectList(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getTargetType, 2)
                .eq(Follow::getTargetId, venueId));
        for (Follow follow : follows) {
            Message msg = new Message();
            msg.setUserId(follow.getUserId());
            msg.setType(1);
            msg.setTitle("放鱼通知");
            msg.setContent("您关注的钓场有新的放鱼视频，快来看看吧！");
            msg.setRefType("venue");
            msg.setRefId(venueId);
            messageDao.insert(msg);
        }
    }

    public void deleteVideo(int videoId) {
        fishVideoDao.deleteById(videoId);
    }

    public FishVideo getVideoById(int videoId) {
        return fishVideoDao.selectById(videoId);
    }

    public void likeVideo(int videoId) {
        FishVideo video = fishVideoDao.selectById(videoId);
        if (video != null) {
            video.setLikeCount(video.getLikeCount() + 1);
            fishVideoDao.updateById(video);
        }
    }
}
