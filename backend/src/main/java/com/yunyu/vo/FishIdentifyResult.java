package com.yunyu.vo;

import lombok.Data;

@Data
public class FishIdentifyResult {
    private String fishName;
    private Double confidence;
    private String description;
    private String fishingTips;
}
