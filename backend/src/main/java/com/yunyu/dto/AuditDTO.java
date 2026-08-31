package com.yunyu.dto;

import lombok.Data;

@Data
public class AuditDTO {
    private String targetType;
    private Integer targetId;
    private String action;
    private String reason;
}
