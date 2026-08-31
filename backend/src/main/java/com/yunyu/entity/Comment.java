package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer postId;
    private Integer userId;
    private Integer parentId;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
