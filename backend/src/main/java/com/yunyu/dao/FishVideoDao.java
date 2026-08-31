package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yunyu.entity.FishVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FishVideoDao extends BaseMapper<FishVideo> {

    @Select("SELECT * FROM t_fish_video WHERE venue_id = #{venueId} AND type = 1 ORDER BY create_time DESC LIMIT 1")
    FishVideo getLatestStockVideo(@Param("venueId") Integer venueId);

    @Select("SELECT * FROM t_fish_video WHERE venue_id = #{venueId} AND type = 2 ORDER BY create_time DESC")
    IPage<FishVideo> getCatchVideoPage(IPage<FishVideo> page, @Param("venueId") Integer venueId);
}
