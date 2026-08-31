package com.yunyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer category;
    private String brand;
    private BigDecimal price;
    private Integer pointsPrice;
    private Integer stock;
    private String images;
    private String description;
    private Integer salesCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
