package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer type;
    private String title;
    private String content;
    private String refType;
    private Integer refId;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
