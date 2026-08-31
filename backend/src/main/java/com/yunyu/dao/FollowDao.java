package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyu.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FollowDao extends BaseMapper<Follow> {

    @Select("SELECT COUNT(*) FROM t_follow WHERE target_type = 2 AND target_id = #{venueId}")
    Integer countByVenue(@Param("venueId") Integer venueId);

    @Select("SELECT * FROM t_follow WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} LIMIT 1")
    Follow checkFollow(@Param("userId") Integer userId,
                       @Param("targetType") Integer targetType,
                       @Param("targetId") Integer targetId);
}
