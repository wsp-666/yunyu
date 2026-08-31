package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_fish_video")
public class FishVideo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer venueId;
    private Integer type;
    private String videoUrl;
    private String coverUrl;
    private Integer fishCount;
    private String fishSpecies;
    private String fishSizeDesc;
    private LocalDateTime fishingTime;
    private BigDecimal ticketPrice;
    private Integer likeCount;
    private Integer uploadUserId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
