package com.yunyu.vo;

import java.util.List;

/**
 * 管理后台数据概览 — 四个图表数据
 */
public class DashboardChartsVO {

    /** 年龄分布 */
    private List<AgeItem> ageDistribution;
    /** 主要鱼种分布 */
    private List<FishSpeciesItem> fishSpeciesDistribution;
    /** 地域分布 */
    private List<RegionItem> regionDistribution;
    /** 钓法分布 */
    private List<MethodItem> methodDistribution;

    // ===== 内部类 =====

    public static class AgeItem {
        private String name;   // 年龄段，如 "18-25岁"
        private Integer value; // 人数
        public AgeItem() {}
        public AgeItem(String name, Integer value) { this.name = name; this.value = value; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }

    public static class FishSpeciesItem {
        private String name;   // 鱼种名，如 "鲫鱼"
        private Integer value; // 出现次数
        public FishSpeciesItem() {}
        public FishSpeciesItem(String name, Integer value) { this.name = name; this.value = value; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }

    public static class RegionItem {
        private String name;   // 城市名，如 "北京"
        private Integer value; // 钓场数量
        public RegionItem() {}
        public RegionItem(String name, Integer value) { this.name = name; this.value = value; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }

    public static class MethodItem {
        private String name;   // 钓法/类型名
        private Integer value; // 数量
        public MethodItem() {}
        public MethodItem(String name, Integer value) { this.name = name; this.value = value; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }

    // ===== getter / setter =====

    public List<AgeItem> getAgeDistribution() { return ageDistribution; }
    public void setAgeDistribution(List<AgeItem> ageDistribution) { this.ageDistribution = ageDistribution; }

    public List<FishSpeciesItem> getFishSpeciesDistribution() { return fishSpeciesDistribution; }
    public void setFishSpeciesDistribution(List<FishSpeciesItem> fishSpeciesDistribution) { this.fishSpeciesDistribution = fishSpeciesDistribution; }

    public List<RegionItem> getRegionDistribution() { return regionDistribution; }
    public void setRegionDistribution(List<RegionItem> regionDistribution) { this.regionDistribution = regionDistribution; }

    public List<MethodItem> getMethodDistribution() { return methodDistribution; }
    public void setMethodDistribution(List<MethodItem> methodDistribution) { this.methodDistribution = methodDistribution; }
}
