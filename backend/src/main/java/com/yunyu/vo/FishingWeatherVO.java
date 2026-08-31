package com.yunyu.vo;

import lombok.Data;

@Data
public class FishingWeatherVO {
    private String city;
    private String temperature;
    private String weatherDesc;
    private String humidity;
    private String wind;
    private String feelsLike;
    private String pressure;
    private String analysis;
}
