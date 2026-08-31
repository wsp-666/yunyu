package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderNo;
    private Integer userId;
    private Integer sessionId;
    private Integer venueId;
    private Integer productId;
    private Integer seatNo;
    private BigDecimal amount;
    private Integer payStatus;
    private LocalDateTime payTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal catchWeight;
    private BigDecimal returnAmount;
    private BigDecimal totalFee;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
