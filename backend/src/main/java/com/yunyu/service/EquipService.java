package com.yunyu.service;

import com.yunyu.dto.EquipDTO;
import com.yunyu.vo.EquipPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EquipService {

    @Autowired
    private DeepSeekService deepSeekService;

    public EquipPlanVO generatePlan(EquipDTO dto) {
        EquipPlanVO plan = new EquipPlanVO();
        plan.setTargetFish(dto.getTargetFish());
        plan.setFishingMethod(dto.getFishingMethod());

        try {
            String systemPrompt = "你是一个专业的钓鱼装备顾问，拥有丰富的淡水钓、路亚、海钓等各类型钓鱼装备知识。\n"
                    + "请根据用户的需求（目标鱼种、钓法、预算），为用户推荐一套完整的钓鱼装备方案。\n"
                    + "推荐应包含以下类别：鱼竿、鱼线、鱼轮（如需）、鱼漂（如需）、鱼饵/拟饵、配件。\n"
                    + "对每个装备给出品牌型号建议、大概价格，并简要说明理由。\n"
                    + "最后给出方案总预算估算。用清晰的分段格式输出，300字以内。";

            String userPrompt = "目标鱼种：" + dto.getTargetFish() + "\n"
                    + "钓法：" + dto.getFishingMethod() + "\n"
                    + "预算范围：" + dto.getBudgetMin() + " - " + dto.getBudgetMax() + "元\n"
                    + "请为我推荐一套完整的装备方案。";

            plan.setAnalysis(deepSeekService.chat(systemPrompt, userPrompt));
        } catch (Exception e) {
            plan.setAnalysis("AI推荐服务暂不可用，请稍后重试。\n\n"
                    + "建议根据目标鱼种【" + dto.getTargetFish() + "】和钓法【" + dto.getFishingMethod()
                    + "】前往商城选购对应品类的装备。");
        }

        return plan;
    }
}
