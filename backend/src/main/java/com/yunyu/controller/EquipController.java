package com.yunyu.controller;

import com.yunyu.common.Result;
import com.yunyu.dto.EquipDTO;
import com.yunyu.service.EquipService;
import com.yunyu.vo.EquipPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equip")
public class EquipController {

    @Autowired
    private EquipService equipService;

    @GetMapping("/recommend")
    public Result<EquipPlanVO> recommendEquip(
            @RequestParam String targetFish,
            @RequestParam int budgetMin,
            @RequestParam int budgetMax,
            @RequestParam String method) {
        EquipDTO dto = new EquipDTO();
        dto.setTargetFish(targetFish);
        dto.setBudgetMin(budgetMin);
        dto.setBudgetMax(budgetMax);
        dto.setFishingMethod(method);
        EquipPlanVO plan = equipService.generatePlan(dto);
        return Result.success(plan);
    }
}
