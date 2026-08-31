package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyu.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDao extends BaseMapper<Admin> {
}
