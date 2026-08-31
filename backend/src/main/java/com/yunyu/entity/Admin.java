package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String account;
    private String password;
    private String name;
    private Integer role;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
