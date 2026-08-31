package com.yunyu.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RevenueStatsVO {
    private List<RevenueData> revenueData;
    private List<SessionStats> sessionStats;

    @Data
    public static class RevenueData {
        private String date;
        private BigDecimal amount;
        private Integer orderCount;
    }

    @Data
    public static class SessionStats {
        private String sessionName;
        private BigDecimal revenue;
        private Integer totalSeats;
        private Integer bookedSeats;
        private Double occupancyRate;
    }
}
