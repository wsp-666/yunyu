package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_points_log")
public class PointsLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer points;
    private Integer type;
    private Integer refId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
