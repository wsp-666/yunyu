package com.yunyu.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VideoDTO {
    private Integer venueId;
    private Integer type;
    private Integer fishCount;
    private String fishSpecies;
    private String fishSizeDesc;
    private LocalDateTime fishingTime;
    private BigDecimal ticketPrice;
}
