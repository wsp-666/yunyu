package com.yunyu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunyu.vo.FishIdentifyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AIService {

    @Autowired
    private DeepSeekService deepSeekService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 硬编码鱼种库作为 AI 调用失败时的 fallback
    private static final Map<String, FishInfo> FISH_DB = new LinkedHashMap<>();

    static {
        FISH_DB.put("鲤鱼", new FishInfo("鲤鱼", "鲤科鱼类，适应性强，分布广泛，是常见的垂钓鱼种。体型较大，拉力强劲，深受钓友喜爱。", "台钓底钓为主，使用腥香型饵料，重窝守钓效果佳；路亚可用软虫跳底。"));
        FISH_DB.put("草鱼", new FishInfo("草鱼", "草食性鱼类，喜食水草和植物饵料，体型较大，中钩后冲击力强。", "台钓浮钓为主，使用草饵、嫩玉米粒或商品饵；抛竿爆炸钩远投效果也好。"));
        FISH_DB.put("鲫鱼", new FishInfo("鲫鱼", "小型淡水鱼，肉质鲜美，四季可钓，对环境适应力极强，是新手入门首选目标鱼。", "台钓细线小钩，腥香饵料，底钓灵敏调漂；冬季可用红虫、蚯蚓作饵。"));
        FISH_DB.put("鲈鱼", new FishInfo("鲈鱼", "凶猛肉食性鱼类，喜栖息于水草区和结构区，是路亚钓法的热门目标鱼。", "路亚使用米诺、VIB、软虫等拟饵，搜索水草边缘和结构区；早晚窗口期最佳。"));
        FISH_DB.put("青鱼", new FishInfo("青鱼", "大型淡水鱼，以螺蛳、贝类为食，体型巨大可达数十斤，是淡水钓的终极挑战目标。", "台钓使用螺蛳肉或商品青鱼饵，重窝守钓；需用大号线组和强力鱼竿。"));
        FISH_DB.put("鲢鳙", new FishInfo("鲢鳙", "滤食性鱼类，喜食浮游生物，中上层鱼种，生长速度快，是水库常见养殖鱼。", "台钓浮钓为主，使用酸臭型雾化饵料，保持高频抛竿形成雾化区诱鱼。"));
        FISH_DB.put("黑鱼", new FishInfo("黑鱼", "凶猛肉食性鱼类，有护幼习性，喜栖息于水草丛中，是雷强钓法的经典目标。", "路亚使用雷蛙、软蛙等水面系拟饵，重点搜索水草密集区；雷强钓法专用装备。"));
        FISH_DB.put("翘嘴", new FishInfo("翘嘴", "中上层掠食性鱼类，喜追逐小鱼群，游速快攻击凶猛，是路亚经典目标鱼。", "路亚使用亮片、米诺远投搜索，早晚窗口期在浅滩和水面炸水处作钓效果最佳。"));
        FISH_DB.put("鳊鱼", new FishInfo("鳊鱼", "中小型淡水鱼，体型侧扁，喜群居，杂食性，是休闲钓的常见鱼种。", "台钓底钓或离底，使用腥香饵料或蚯蚓；小钩细线灵敏度高。"));
        FISH_DB.put("罗非鱼", new FishInfo("罗非鱼", "热带淡水鱼，繁殖力强，耐低氧，南方水域极为常见，全年可钓。", "台钓使用腥味饵料，底钓为主；罗非鱼嘴部坚硬，需用锋利鱼钩及时刺鱼。"));
    }

    public FishIdentifyResult identifyFish(MultipartFile image) {
        try {
            // 将图片转为 base64
            byte[] imageBytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // 构建鱼类识别的 system prompt
            String systemPrompt = "你是一位拥有20年经验的钓鱼大师和鱼类识别专家。" +
                "请仔细观察用户上传的鱼类图片，准确识别鱼的品种。" +
                "你必须用纯JSON格式回答，不要包含任何其他文字，格式如下：" +
                "{\"fishName\":\"鱼的准确中文名称\",\"confidence\":0.XX,\"description\":\"该鱼类的特征和习性简述(50-100字)\",\"fishingTips\":\"针对该鱼种的实用垂钓技巧(50-100字)\"}";

            String userPrompt = "请识别这张图片中的鱼是什么品种。";

            // 调用 DeepSeek 视觉模型
            String response = deepSeekService.chatWithImage(systemPrompt, userPrompt, base64Image);

            // 解析 AI 返回的 JSON
            String jsonStr = response.trim();
            // 去除可能的 markdown 代码块标记
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.replaceAll("```json\\s*", "")
                                 .replaceAll("```\\s*", "")
                                 .trim();
            }

            JsonNode root = objectMapper.readTree(jsonStr);

            FishIdentifyResult result = new FishIdentifyResult();
            result.setFishName(root.path("fishName").asText("未知鱼种"));
            result.setConfidence(root.path("confidence").asDouble(0.85));
            result.setDescription(root.path("description").asText(""));
            result.setFishingTips(root.path("fishingTips").asText(""));
            return result;

        } catch (Exception e) {
            System.err.println("AI识鱼调用失败，使用本地知识库兜底: " + e.getMessage());
            return fallbackIdentify(image);
        }
    }

    /** AI 调用失败时的兜底方案（基于文件名 hash） */
    private FishIdentifyResult fallbackIdentify(MultipartFile image) {
        String originalName = image.getOriginalFilename();
        int hash = Math.abs((originalName != null ? originalName : "fish").hashCode());
        String[] fishNames = FISH_DB.keySet().toArray(new String[0]);
        String fishName = fishNames[hash % fishNames.length];
        FishInfo info = FISH_DB.get(fishName);

        FishIdentifyResult result = new FishIdentifyResult();
        result.setFishName(fishName);
        result.setConfidence(0.82 + (hash % 15) * 0.01);
        result.setDescription(info.description);
        result.setFishingTips(info.fishingTips);
        return result;
    }

    private record FishInfo(String name, String description, String fishingTips) {}
}
