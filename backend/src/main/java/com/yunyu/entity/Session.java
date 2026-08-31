package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_session")
public class Session {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer venueId;
    private String name;
    private BigDecimal ticketPrice;
    private Integer totalSeats;
    private Integer remainSeats;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime drawTime;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
