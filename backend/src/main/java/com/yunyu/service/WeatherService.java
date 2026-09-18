package com.yunyu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunyu.common.CacheKeys;
import com.yunyu.vo.FishingWeatherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class WeatherService {

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private RedisCacheService redisCacheService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    public FishingWeatherVO getFishingWeather(String city) {
        String key = CacheKeys.weather(city);
        return redisCacheService.getOrLoad(key, Duration.ofMinutes(30), FishingWeatherVO.class,
                () -> fetchWeather(city));
    }

    private FishingWeatherVO fetchWeather(String city) {
        FishingWeatherVO vo = new FishingWeatherVO();
        vo.setCity(city);

        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = "https://wttr.in/" + encodedCity + "?format=j1";
            String weatherJson = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(weatherJson);
            JsonNode current = root.path("current_condition").get(0);

            vo.setTemperature(current.path("temp_C").asText("--") + "°C");
            vo.setWeatherDesc(current.path("weatherDesc").get(0).path("value").asText("--"));
            vo.setHumidity(current.path("humidity").asText("--") + "%");
            vo.setWind(current.path("winddir16Point").asText("--")
                    + " " + current.path("windspeedKmph").asText("--") + "km/h");
            vo.setFeelsLike(current.path("feelsLikeC").asText("--") + "°C");
            vo.setPressure(current.path("pressure").asText("--") + "hPa");
        } catch (Exception e) {
            System.err.println("Weather API error: " + e.getMessage());
            vo.setTemperature("--°C");
            vo.setWeatherDesc("获取失败");
            vo.setHumidity("--%");
            vo.setWind("--");
            vo.setFeelsLike("--°C");
            vo.setPressure("--hPa");
        }

        try {
            String systemPrompt = "你是一个专业的钓鱼天气分析专家。"
                    + "请根据城市的实际天气数据，给出简洁的钓鱼适宜度分析和出钓建议。"
                    + "用自然段落输出，控制在150字以内。";
            String userPrompt = "城市：" + city + "\n"
                    + "当前温度：" + vo.getTemperature() + "\n"
                    + "天气：" + vo.getWeatherDesc() + "\n"
                    + "湿度：" + vo.getHumidity() + "\n"
                    + "风力：" + vo.getWind() + "\n"
                    + "体感温度：" + vo.getFeelsLike() + "\n"
                    + "气压：" + vo.getPressure() + "\n"
                    + "请分析这个天气是否适合钓鱼，给出建议。";

            vo.setAnalysis(deepSeekService.chat(systemPrompt, userPrompt));
        } catch (Exception e) {
            vo.setAnalysis("AI分析服务暂不可用，建议查看天气数据自行判断：气压稳定、风力适中、温差小时较适宜出钓。");
        }

        return vo;
    }
}
