package com.yunyu.vo;

import com.yunyu.entity.FishVideo;
import com.yunyu.entity.Session;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VenueDetailVO {
    private Integer id;
    private String name;
    private String address;
    private String phone;
    private String coverImage;
    private String seatMap;
    private String images;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String ruleDesc;
    private Integer totalSeats;
    private Integer followerCount;
    private Integer status;
    private FishVideo latestStockVideo;
    private List<FishVideo> catchVideoList;
    private List<Session> sessionList;
}
