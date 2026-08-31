package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer targetType;
    private Integer targetId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
