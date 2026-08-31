package com.yunyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yunyu.entity.FishingVenue;
import com.yunyu.vo.VenueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FishingVenueDao extends BaseMapper<FishingVenue> {

    @Select("SELECT v.*, " +
            "       fv.create_time AS latestStockTime, " +
            "       fv.fish_species AS latestFishSpecies, " +
            "       fv.fish_count AS latestFishCount " +
            "FROM t_fishing_venue v " +
            "LEFT JOIN t_fish_video fv ON v.id = fv.venue_id AND fv.type = 1 " +
            "WHERE v.status = 1 " +
            "AND (#{keyword} IS NULL OR v.name LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY fv.create_time DESC")
    IPage<VenueVO> selectVenuePage(IPage<VenueVO> page, @Param("keyword") String keyword);

    @Select("SELECT v.*, " +
            "       fv.create_time AS latestStockTime, " +
            "       fv.fish_species AS latestFishSpecies, " +
            "       fv.fish_count AS latestFishCount " +
            "FROM t_fishing_venue v " +
            "LEFT JOIN t_fish_video fv ON v.id = fv.venue_id AND fv.type = 1 " +
            "WHERE v.status = 1 " +
            "ORDER BY fv.create_time DESC")
    List<VenueVO> selectAllVenues();
}
