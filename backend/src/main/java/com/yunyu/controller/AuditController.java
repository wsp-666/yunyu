package com.yunyu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.common.PageResult;
import com.yunyu.common.Result;
import com.yunyu.dto.AuditDTO;
import com.yunyu.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/pending")
    public Result<PageResult<?>> getPendingList(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<?> pendingPage = auditService.getPendingList(type, page, size);
        return Result.success(PageResult.from(pendingPage));
    }

    @PostMapping("/do")
    public Result<Void> audit(@RequestBody AuditDTO dto) {
        auditService.audit(dto);
        return Result.success();
    }
}
