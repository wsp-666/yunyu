package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_fishing_spot")
public class FishingSpot {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer type;
    private String fishSpecies;
    private String waterQuality;
    private String depthDesc;
    private String feeDesc;
    private String envDesc;
    private String navigation;
    private String images;
    private Integer status;
    private String rejectReason;
    private Integer submitUserId;
    private Integer viewCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
