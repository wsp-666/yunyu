package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String account;
    private String password;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer memberLevel;
    private LocalDateTime memberExpire;
    private Integer points;
    private Integer age;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    @TableField("role")
    private Integer role;
    private String lockState;
}
