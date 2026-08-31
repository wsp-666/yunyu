package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyu.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageDao extends BaseMapper<Message> {
}
