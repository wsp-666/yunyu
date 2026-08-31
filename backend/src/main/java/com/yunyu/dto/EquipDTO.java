package com.yunyu.dto;

import lombok.Data;

@Data
public class EquipDTO {
    private String targetFish;
    private Integer budgetMin;
    private Integer budgetMax;
    private String fishingMethod;
}
