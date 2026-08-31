package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyu.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostDao extends BaseMapper<Post> {
}
