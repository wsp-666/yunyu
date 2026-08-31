package com.yunyu.controller;

import com.yunyu.common.Result;
import com.yunyu.service.AIService;
import com.yunyu.service.DeepSeekService;
import com.yunyu.vo.FishIdentifyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/identify")
    public Result<FishIdentifyResult> identifyFish(@RequestParam MultipartFile image) {
        FishIdentifyResult result = aiService.identifyFish(image);
        return Result.success(result);
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }

        String systemPrompt = "你是一个经验丰富的钓鱼大师，拥有20年以上的淡水钓、路亚、海钓经验。\n"
                + "你能解答关于鱼种识别、钓法技巧、装备选择、钓点选择、天气判断、饵料搭配等各类钓鱼问题。\n"
                + "回答风格：专业但通俗易懂，像老师在教导学生一样，偶尔可以分享一些实战经验和小技巧。\n"
                + "每次回答控制在200字以内，简洁有力。";

        try {
            String reply = deepSeekService.chat(systemPrompt, message);
            Map<String, Object> result = new HashMap<>();
            result.put("reply", reply);
            result.put("role", "master");
            return Result.success(result);
        } catch (Exception e) {
            System.err.println("AI Chat error: " + e.getMessage());
            e.printStackTrace();
            return Result.error("AI大师暂时无法回复：" + e.getMessage());
        }
    }
}
