package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyu.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductDao extends BaseMapper<Product> {

    @Select("SELECT * FROM t_product WHERE category = #{category} AND status = 1 ORDER BY price ASC")
    List<Product> selectByCategory(@Param("category") Integer category);
}
