package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_fishing_venue")
public class FishingVenue {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer ownerId;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String phone;
    private String coverImage;
    private String seatMap;
    private String images;
    private String ruleDesc;
    private Integer totalSeats;
    private Integer followerCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
