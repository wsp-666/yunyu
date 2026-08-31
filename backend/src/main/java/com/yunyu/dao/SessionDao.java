package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyu.entity.Session;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SessionDao extends BaseMapper<Session> {

    @Select("SELECT * FROM t_session WHERE venue_id = #{venueId} AND status IN (1,2) ORDER BY start_time ASC")
    List<Session> getAvailableSessions(@Param("venueId") Integer venueId);
}
