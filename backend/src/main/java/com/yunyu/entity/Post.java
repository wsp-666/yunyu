package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer type;
    private String title;
    private String content;
    private String images;
    private String videoUrl;
    private Integer spotId;
    private Integer venueId;
    private String topicTag;
    private String weatherInfo;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
