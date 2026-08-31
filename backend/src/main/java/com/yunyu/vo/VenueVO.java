package com.yunyu.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VenueVO {
    private Integer id;
    private String name;
    private String address;
    private String coverImage;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer totalSeats;
    private Integer followerCount;
    private Integer status;
    private LocalDateTime latestStockTime;
    private String latestFishSpecies;
    private Integer latestFishCount;
    private Boolean isNew;
}
