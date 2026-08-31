package com.yunyu.controller;

import com.yunyu.common.Result;
import com.yunyu.service.WeatherService;
import com.yunyu.vo.FishingWeatherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/fishing")
    public Result<FishingWeatherVO> getFishingWeather(@RequestParam String city) {
        FishingWeatherVO vo = weatherService.getFishingWeather(city);
        return Result.success(vo);
    }
}
